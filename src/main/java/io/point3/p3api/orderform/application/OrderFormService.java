package io.point3.p3api.orderform.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.orderform.application.create.CreateOrderFormCommand;
import io.point3.p3api.orderform.application.create.OrderFormCreateUseCase;
import io.point3.p3api.orderform.application.port.OrderFormPersistencePort;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.orderform.application.update.OrderFormUpdateUseCase;
import io.point3.p3api.orderform.application.update.UpdateOrderFormCommand;
import io.point3.p3api.orderform.domain.entity.OrderFormField;
import io.point3.p3api.orderform.domain.entity.OrderFormFieldGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormFieldOption;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderFormService
    implements OrderFormCreateUseCase, OrderFormUpdateUseCase, OrderFormQueryUseCase {
  private final OrderFormPersistencePort orderFormPersistencePort;
  private final ObjectMapper objectMapper;

  @Override
  public OrderFormResult create(CreateOrderFormCommand command) {
    if (orderFormPersistencePort.existsActiveTemplateByStoreId(command.storeId())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_ACTIVE_ALREADY_EXISTS);
    }
    validateFields(command.fields());
    OrderFormTemplate template = orderFormPersistencePort.saveTemplate(
        OrderFormTemplate.create(command.storeId(), command.name()));
    saveDefinition(template.getId(), command.fields());
    return getResult(template);
  }

  @Override
  public OrderFormResult update(UpdateOrderFormCommand command) {
    OrderFormTemplate template = findTemplate(command.storeId(), command.templateId());
    validateFields(command.fields());
    template.updateName(command.name());
    orderFormPersistencePort.deleteGroupsByTemplateId(template.getId());
    saveDefinition(template.getId(), command.fields());
    return getResult(template);
  }

  @Override
  public OrderFormResult inactive(UUID storeId, UUID templateId) {
    OrderFormTemplate template = findTemplate(storeId, templateId);
    template.inactive();
    return getResult(template);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderFormResult getSellerTemplate(UUID storeId, UUID templateId) {
    return getResult(findTemplate(storeId, templateId));
  }

  @Override
  @Transactional(readOnly = true)
  public OrderFormResult getActiveTemplate(UUID storeId) {
    return getResult(orderFormPersistencePort
        .findActiveTemplateByStoreId(storeId)
        .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND)));
  }

  private void saveDefinition(UUID templateId, List<? extends OrderFormFieldCommand> fields) {
    Map<Integer, List<OrderFormFieldCommand>> byGroup = fields.stream()
        .map(field -> (OrderFormFieldCommand) field)
        .collect(Collectors.groupingBy(this::groupSortOrder));
    for (int groupOrder = 0; groupOrder < byGroup.size(); groupOrder++) {
      List<OrderFormFieldCommand> groupFields = byGroup.get(groupOrder);
      OrderFormFieldGroup group = orderFormPersistencePort.saveGroup(OrderFormFieldGroup.create(
          templateId,
          groupTitle(groupFields.getFirst()),
          groupDescription(groupFields.getFirst()),
          groupOrder));
      List<OrderFormField> saved = orderFormPersistencePort.saveFields(groupFields.stream()
          .map(field -> OrderFormField.create(
              group.getId(),
              field.label(),
              field.fieldType(),
              field.required(),
              normalizeSettings(field.fieldType(), field.settings()),
              field.sortOrder()))
          .toList());
      for (int index = 0; index < saved.size(); index++) {
        OrderFormField field = saved.get(index);
        orderFormPersistencePort.saveOptions(groupFields.get(index).options().stream()
            .map(option -> createOption(field.getId(), option))
            .toList());
      }
    }
  }

  private OrderFormFieldOption createOption(UUID fieldId, OrderFormFieldOptionCommand option) {
    OrderFormFieldOption result =
        OrderFormFieldOption.create(fieldId, option.label(), option.value(), option.sortOrder());
    if (!option.active()) {
      result.inactive();
    }
    return result;
  }

  private OrderFormResult getResult(OrderFormTemplate template) {
    List<OrderFormFieldGroup> groups =
        orderFormPersistencePort.findGroupsByTemplateId(template.getId());
    List<OrderFormField> fields = orderFormPersistencePort.findFieldsByTemplateId(template.getId());
    return OrderFormResult.from(
        template,
        groups,
        fields,
        orderFormPersistencePort.findOptionsByFieldIds(
            fields.stream().map(OrderFormField::getId).toList()));
  }

  private OrderFormTemplate findTemplate(UUID storeId, UUID templateId) {
    return orderFormPersistencePort
        .findTemplateByIdAndStoreId(templateId, storeId)
        .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND));
  }

  private void validateFields(List<? extends OrderFormFieldCommand> fields) {
    if (fields == null || fields.isEmpty()) {
      throw invalid();
    }
    Map<Integer, List<OrderFormFieldCommand>> groups = fields.stream()
        .map(field -> (OrderFormFieldCommand) field)
        .collect(Collectors.groupingBy(this::groupSortOrder));
    for (int groupOrder = 0; groupOrder < groups.size(); groupOrder++) {
      List<OrderFormFieldCommand> groupFields = groups.get(groupOrder);
      if (groupFields == null || groupFields.isEmpty()) {
        throw invalid();
      }
      OrderFormFieldCommand first = groupFields.getFirst();
      if (groupTitle(first) == null || groupTitle(first).isBlank()) {
        throw invalid();
      }
      for (int index = 0; index < groupFields.size(); index++) {
        OrderFormFieldCommand field = groupFields.get(index);
        if (field.sortOrder() != index
            || !groupTitle(first).equals(groupTitle(field))
            || !java.util.Objects.equals(groupDescription(first), groupDescription(field))) {
          throw invalid();
        }
        validateOptions(field.fieldType(), field.options());
        normalizeSettings(field.fieldType(), field.settings());
      }
    }
  }

  private void validateOptions(FieldType type, List<OrderFormFieldOptionCommand> options) {
    boolean selectable = type == FieldType.SINGLE_SELECT || type == FieldType.MULTI_SELECT;
    if (selectable != !options.isEmpty()) {
      throw invalid();
    }
    for (int index = 0; index < options.size(); index++) {
      OrderFormFieldOptionCommand option = options.get(index);
      if (option.sortOrder() != index
          || option.label() == null
          || option.label().isBlank()
          || option.value() == null
          || option.value().isBlank()) {
        throw invalid();
      }
    }
  }

  private String normalizeSettings(FieldType type, String settings) {
    if (settings == null || settings.isBlank()) {
      return null;
    }
    try {
      JsonNode node = objectMapper.readTree(settings);
      if (!node.isObject()) {
        throw invalid();
      }
      ObjectNode accepted = objectMapper.createObjectNode();
      switch (type) {
        case TEXT, TEXTAREA -> {
          copyText(node, accepted, "placeholder");
          copyPositiveInteger(node, accepted, "maxLength");
        }
        case NUMBER -> copyNumberSettings(node, accepted);
        case DATE, TIME, DATETIME -> copyDateSettings(node, accepted);
        case IMAGE -> copyImageSettings(node, accepted);
        case SINGLE_SELECT, MULTI_SELECT -> {}
      }
      return accepted.isEmpty() ? null : objectMapper.writeValueAsString(accepted);
    } catch (JsonProcessingException exception) {
      throw invalid();
    }
  }

  private void copyNumberSettings(JsonNode node, ObjectNode accepted) {
    copyNumber(node, accepted, "min");
    copyNumber(node, accepted, "max");
    copyPositiveNumber(node, accepted, "step");
    if (accepted.has("min")
        && accepted.has("max")
        && new BigDecimal(accepted.get("min").asText())
                .compareTo(new BigDecimal(accepted.get("max").asText()))
            > 0) {
      throw invalid();
    }
  }

  private void copyDateSettings(JsonNode node, ObjectNode accepted) {
    copyText(node, accepted, "min");
    copyText(node, accepted, "max");
    if (accepted.has("min")
        && accepted.has("max")
        && accepted.get("min").asText().compareTo(accepted.get("max").asText()) > 0) {
      throw invalid();
    }
  }

  private void copyImageSettings(JsonNode node, ObjectNode accepted) {
    copyPositiveInteger(node, accepted, "maxCount");
    if (accepted.has("maxCount") && accepted.get("maxCount").asInt() > 5) {
      throw invalid();
    }
    JsonNode types = node.get("allowedContentTypes");
    if (types != null) {
      if (!types.isArray() || types.isEmpty()) {
        throw invalid();
      }
      for (JsonNode type : types) {
        if (!type.isTextual()
            || !List.of("image/jpeg", "image/png", "image/webp").contains(type.asText())) {
          throw invalid();
        }
      }
      accepted.set("allowedContentTypes", types);
    }
  }

  private void copyText(JsonNode source, ObjectNode target, String name) {
    JsonNode value = source.get(name);
    if (value != null) {
      if (!value.isTextual()) {
        throw invalid();
      }
      target.set(name, value);
    }
  }

  private void copyNumber(JsonNode source, ObjectNode target, String name) {
    JsonNode value = source.get(name);
    if (value != null) {
      if (!value.isNumber()) {
        throw invalid();
      }
      target.set(name, value);
    }
  }

  private void copyPositiveNumber(JsonNode source, ObjectNode target, String name) {
    copyNumber(source, target, name);
    if (target.has(name) && target.get(name).decimalValue().signum() <= 0) {
      throw invalid();
    }
  }

  private void copyPositiveInteger(JsonNode source, ObjectNode target, String name) {
    JsonNode value = source.get(name);
    if (value != null) {
      if (!value.canConvertToInt() || value.asInt() < 1) {
        throw invalid();
      }
      target.set(name, value);
    }
  }

  private int groupSortOrder(OrderFormFieldCommand field) {
    return field instanceof CreateOrderFormCommand.Field create
        ? create.groupSortOrder()
        : ((UpdateOrderFormCommand.Field) field).groupSortOrder();
  }

  private String groupTitle(OrderFormFieldCommand field) {
    return field instanceof CreateOrderFormCommand.Field create
        ? create.groupTitle()
        : ((UpdateOrderFormCommand.Field) field).groupTitle();
  }

  private String groupDescription(OrderFormFieldCommand field) {
    return field instanceof CreateOrderFormCommand.Field create
        ? create.groupDescription()
        : ((UpdateOrderFormCommand.Field) field).groupDescription();
  }

  private BaseException invalid() {
    return new BaseException(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID);
  }
}
