package io.point3.p3api.orderform.controller.response;

import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.UUID;

public record OrderFormFieldResponse(
    UUID id,
    UUID templateId,
    String label,
    FieldType fieldType,
    boolean required,
    String settings,
    int sortOrder) {

  public static OrderFormFieldResponse from(OrderFormFieldResult result) {
    return new OrderFormFieldResponse(
        result.id(),
        result.templateId(),
        result.label(),
        result.fieldType(),
        result.required(),
        result.settings(),
        result.sortOrder());
  }
}
