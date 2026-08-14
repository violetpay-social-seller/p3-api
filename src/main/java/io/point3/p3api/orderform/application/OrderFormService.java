package io.point3.p3api.orderform.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import java.util.List;
import java.util.UUID;
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

    OrderFormTemplate template =
        orderFormPersistencePort.saveTemplate(
            OrderFormTemplate.create(command.storeId(), command.name()));
    List<OrderFormField> fields =
        orderFormPersistencePort.saveFields(toFields(template.getId(), command.fields()));

    return OrderFormResult.from(template, fields);
  }

  @Override
  public OrderFormResult update(UpdateOrderFormCommand command) {
    OrderFormTemplate template = findTemplate(command.storeId(), command.templateId());
    validateFields(command.fields());

    template.updateName(command.name());
    orderFormPersistencePort.deleteFieldsByTemplateId(template.getId());
    List<OrderFormField> fields =
        orderFormPersistencePort.saveFields(toFields(template.getId(), command.fields()));

    return OrderFormResult.from(template, fields);
  }

  @Override
  public OrderFormResult inactive(UUID storeId, UUID templateId) {
    OrderFormTemplate template = findTemplate(storeId, templateId);
    template.inactive();

    return OrderFormResult.from(
        template, orderFormPersistencePort.findFieldsByTemplateId(template.getId()));
  }

  @Override
  @Transactional(readOnly = true)
  public OrderFormResult getSellerTemplate(UUID storeId, UUID templateId) {
    OrderFormTemplate template = findTemplate(storeId, templateId);
    return OrderFormResult.from(
        template, orderFormPersistencePort.findFieldsByTemplateId(template.getId()));
  }

  @Override
  @Transactional(readOnly = true)
  public OrderFormResult getActiveTemplate(UUID storeId) {
    OrderFormTemplate template = orderFormPersistencePort
        .findActiveTemplateByStoreId(storeId)
        .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND));

    return OrderFormResult.from(
        template, orderFormPersistencePort.findFieldsByTemplateId(template.getId()));
  }

  private OrderFormTemplate findTemplate(UUID storeId, UUID templateId) {
    return orderFormPersistencePort
        .findTemplateByIdAndStoreId(templateId, storeId)
        .orElseThrow(() -> new BaseException(OrderFormErrorCode.ORDER_FORM_NOT_FOUND));
  }

  private void validateFields(List<? extends OrderFormFieldCommand> fields) {
    if (fields == null || fields.isEmpty()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }

    for (int index = 0; index < fields.size(); index++) {
      OrderFormFieldCommand field = fields.get(index);
      if (field.sortOrder() != index) {
        throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
      }
      validateSettings(field.settings());
    }
  }

  private void validateSettings(String settings) {
    if (settings == null || settings.isBlank()) {
      return;
    }

    try {
      JsonNode node = objectMapper.readTree(settings);
      if (!node.isObject()) {
        throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
      }
    } catch (JsonProcessingException e) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }
  }

  private List<OrderFormField> toFields(
      UUID templateId, List<? extends OrderFormFieldCommand> fields) {
    return fields.stream()
        .map(field -> OrderFormField.create(
            templateId,
            field.label(),
            field.fieldType(),
            field.required(),
            normalizeSettings(field.settings()),
            field.sortOrder()))
        .toList();
  }

  private String normalizeSettings(String settings) {
    if (settings == null || settings.isBlank()) {
      return null;
    }

    return settings;
  }
}
