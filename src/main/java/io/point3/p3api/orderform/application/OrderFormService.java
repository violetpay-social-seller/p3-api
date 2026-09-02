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
import io.point3.p3api.orderform.domain.entity.OrderFormCategoryGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormOption;
import io.point3.p3api.orderform.domain.entity.OrderFormOptionGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import io.point3.p3api.orderform.domain.type.OptionInputType;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import io.point3.p3api.orderform.domain.type.SelectionType;
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
  private static final int MAX_OPTIONS = 6;
  private static final int MAX_IMAGE_COUNT = 5;
  private static final int MAX_PRICE_LABEL_LENGTH = 100;

  private final OrderFormPersistencePort orderFormPersistencePort;
  private final ObjectMapper objectMapper;

  @Override
  public OrderFormResult create(CreateOrderFormCommand command) {
    if (orderFormPersistencePort.existsActiveTemplateByStoreId(command.storeId())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_ACTIVE_ALREADY_EXISTS);
    }
    validateOptionGroups(command.optionGroups());
    OrderFormTemplate template = orderFormPersistencePort.saveTemplate(
        OrderFormTemplate.create(command.storeId(), command.name()));
    saveDefinition(template.getId(), command.optionGroups());
    return getResult(template);
  }

  @Override
  public OrderFormResult update(UpdateOrderFormCommand command) {
    OrderFormTemplate template = findTemplate(command.storeId(), command.templateId());
    validateOptionGroups(command.optionGroups());
    template.updateName(command.name());
    orderFormPersistencePort.deleteCategoryGroupsByTemplateId(template.getId());
    saveDefinition(template.getId(), command.optionGroups());
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

  private void saveDefinition(
      UUID templateId, List<? extends OrderFormOptionGroupCommand> optionGroups) {
    Map<Integer, List<OrderFormOptionGroupCommand>> byGroup = optionGroups.stream()
        .map(optionGroup -> (OrderFormOptionGroupCommand) optionGroup)
        .collect(Collectors.groupingBy(this::groupSortOrder));
    for (int groupOrder : byGroup.keySet().stream().sorted().toList()) {
      List<OrderFormOptionGroupCommand> categoryOptionGroups = byGroup.get(groupOrder);
      OrderFormCategoryGroup group =
          orderFormPersistencePort.saveCategoryGroup(OrderFormCategoryGroup.create(
              templateId,
              groupCategory(categoryOptionGroups.getFirst()),
              groupTitle(categoryOptionGroups.getFirst()),
              groupDescription(categoryOptionGroups.getFirst()),
              groupOrder));
      List<OrderFormOptionGroup> saved =
          orderFormPersistencePort.saveOptionGroups(categoryOptionGroups.stream()
              .map(optionGroup -> OrderFormOptionGroup.create(
                  group.getId(),
                  optionGroup.label(),
                  optionGroup.selectionType(),
                  optionGroup.required(),
                  optionGroup.sortOrder()))
              .toList());
      for (int index = 0; index < saved.size(); index++) {
        OrderFormOptionGroup optionGroup = saved.get(index);
        orderFormPersistencePort.saveOptions(categoryOptionGroups.get(index).options().stream()
            .map(option -> createOption(optionGroup.getId(), option))
            .toList());
      }
    }
  }

  private OrderFormOption createOption(UUID optionGroupId, OrderFormOptionCommand option) {
    OrderFormOption result = OrderFormOption.create(
        optionGroupId,
        option.label(),
        option.value(),
        option.inputType(),
        option.price(),
        normalizePriceLabel(option.priceLabel()),
        normalizeSettings(option.inputType(), option.settings()),
        option.sortOrder());
    if (!option.active()) {
      result.inactive();
    }
    return result;
  }

  private OrderFormResult getResult(OrderFormTemplate template) {
    List<OrderFormCategoryGroup> groups =
        orderFormPersistencePort.findCategoryGroupsByTemplateId(template.getId());
    List<OrderFormOptionGroup> optionGroups =
        orderFormPersistencePort.findOptionGroupsByTemplateId(template.getId());
    return OrderFormResult.from(
        template,
        groups,
        optionGroups,
        orderFormPersistencePort.findOptionsByOptionGroupIds(
            optionGroups.stream().map(OrderFormOptionGroup::getId).toList()));
  }

  private OrderFormTemplate findTemplate(UUID storeId, UUID templateId) {
    return orderFormPersistencePort
        .findTemplateByIdAndStoreId(templateId, storeId)
        .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND));
  }

  private void validateOptionGroups(List<? extends OrderFormOptionGroupCommand> optionGroups) {
    if (optionGroups == null || optionGroups.isEmpty()) {
      throw invalid();
    }
    Map<Integer, List<OrderFormOptionGroupCommand>> groups = optionGroups.stream()
        .map(optionGroup -> (OrderFormOptionGroupCommand) optionGroup)
        .collect(Collectors.groupingBy(this::groupSortOrder));
    for (int groupOrder : groups.keySet().stream().sorted().toList()) {
      List<OrderFormOptionGroupCommand> categoryOptionGroups = groups.get(groupOrder);
      if (categoryOptionGroups == null || categoryOptionGroups.isEmpty()) {
        throw invalid();
      }
      OrderFormOptionGroupCommand first = categoryOptionGroups.getFirst();
      validateGroup(first, groupOrder);
      for (int index = 0; index < categoryOptionGroups.size(); index++) {
        OrderFormOptionGroupCommand optionGroup = categoryOptionGroups.get(index);
        if (optionGroup.sortOrder() != index
            || groupCategory(first) != groupCategory(optionGroup)
            || !groupTitle(first).equals(groupTitle(optionGroup))
            || !java.util.Objects.equals(groupDescription(first), groupDescription(optionGroup))) {
          throw invalid();
        }
        validateSelectionTypePolicy(groupCategory(optionGroup), optionGroup.selectionType());
        validateOptions(optionGroup.options());
      }
    }
  }

  private void validateGroup(OrderFormOptionGroupCommand optionGroup, int groupOrder) {
    OrderFormCategory category = groupCategory(optionGroup);
    if (category == null
        || category.getSortOrder() != groupOrder
        || groupTitle(optionGroup) == null
        || !category.getTitle().equals(groupTitle(optionGroup))) {
      throw invalid();
    }
  }

  private void validateOptions(List<OrderFormOptionCommand> options) {
    if (options == null || options.isEmpty()) {
      throw invalid();
    }
    if (options.size() > MAX_OPTIONS) {
      throw invalid();
    }
    for (int index = 0; index < options.size(); index++) {
      OrderFormOptionCommand option = options.get(index);
      if (option.sortOrder() != index
          || option.label() == null
          || option.label().isBlank()
          || option.value() == null
          || option.value().isBlank()
          || option.inputType() == null) {
        throw invalid();
      }
      validateOptionPrice(option.inputType(), option.price(), option.priceLabel());
      normalizeSettings(option.inputType(), option.settings());
    }
  }

  private void validateSelectionTypePolicy(
      OrderFormCategory category, SelectionType selectionType) {
    if (selectionType == SelectionType.MULTI && category != OrderFormCategory.CAKE_DESIGN) {
      throw invalid();
    }
  }

  private void validateOptionPrice(OptionInputType type, Long price, String priceLabel) {
    boolean priced = type == OptionInputType.SELECT
        || type == OptionInputType.SELECT_WITH_TEXT
        || type == OptionInputType.TEXT
        || type == OptionInputType.IMAGE;
    String normalizedPriceLabel = normalizePriceLabel(priceLabel);
    if (priced
        && ((price == null) == (normalizedPriceLabel == null) || (price != null && price < 0))) {
      throw invalid();
    }
    if (!priced && (price != null || normalizedPriceLabel != null)) {
      throw invalid();
    }
  }

  private String normalizePriceLabel(String priceLabel) {
    if (priceLabel == null) {
      return null;
    }
    String normalized = priceLabel.trim();
    if (normalized.isEmpty() || normalized.length() > MAX_PRICE_LABEL_LENGTH) {
      throw invalid();
    }
    return normalized;
  }

  private String normalizeSettings(OptionInputType type, String settings) {
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
          copyText(node, accepted, "helperText");
          copyPositiveInteger(node, accepted, "maxLength");
        }
        case IMAGE -> {
          copyImageSettings(node, accepted);
          copyText(node, accepted, "helperText");
        }
        case SELECT_WITH_TEXT -> {
          copyText(node, accepted, "placeholder");
          copyText(node, accepted, "helperText");
        }
        case SELECT -> {}
      }
      return accepted.isEmpty() ? null : objectMapper.writeValueAsString(accepted);
    } catch (JsonProcessingException exception) {
      throw invalid();
    }
  }

  private void copyImageSettings(JsonNode node, ObjectNode accepted) {
    copyPositiveInteger(node, accepted, "maxCount");
    if (accepted.has("maxCount") && accepted.get("maxCount").asInt() > MAX_IMAGE_COUNT) {
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

  private void copyPositiveInteger(JsonNode source, ObjectNode target, String name) {
    JsonNode value = source.get(name);
    if (value != null) {
      if (!value.canConvertToInt() || value.asInt() < 1) {
        throw invalid();
      }
      target.set(name, value);
    }
  }

  private int groupSortOrder(OrderFormOptionGroupCommand optionGroup) {
    return optionGroup instanceof CreateOrderFormCommand.OptionGroup create
        ? create.groupSortOrder()
        : ((UpdateOrderFormCommand.OptionGroup) optionGroup).groupSortOrder();
  }

  private OrderFormCategory groupCategory(OrderFormOptionGroupCommand optionGroup) {
    return optionGroup instanceof CreateOrderFormCommand.OptionGroup create
        ? create.groupCategory()
        : ((UpdateOrderFormCommand.OptionGroup) optionGroup).groupCategory();
  }

  private String groupTitle(OrderFormOptionGroupCommand optionGroup) {
    return optionGroup instanceof CreateOrderFormCommand.OptionGroup create
        ? create.groupTitle()
        : ((UpdateOrderFormCommand.OptionGroup) optionGroup).groupTitle();
  }

  private String groupDescription(OrderFormOptionGroupCommand optionGroup) {
    return optionGroup instanceof CreateOrderFormCommand.OptionGroup create
        ? create.groupDescription()
        : ((UpdateOrderFormCommand.OptionGroup) optionGroup).groupDescription();
  }

  private BaseException invalid() {
    return new BaseException(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID);
  }
}
