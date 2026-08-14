package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormField;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.UUID;

public record OrderFormFieldResult(
    UUID id,
    UUID templateId,
    String label,
    FieldType fieldType,
    boolean required,
    String settings,
    int sortOrder) {

  public static OrderFormFieldResult from(OrderFormField field) {
    return new OrderFormFieldResult(
        field.getId(),
        field.getTemplateId(),
        field.getLabel(),
        field.getFieldType(),
        field.isRequired(),
        field.getSettings(),
        field.getSortOrder());
  }
}
