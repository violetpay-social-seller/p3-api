package io.point3.p3api.orderform.controller.response;

import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.List;
import java.util.UUID;

public record OrderFormFieldResponse(
    UUID id,
    UUID groupId,
    String label,
    FieldType fieldType,
    boolean required,
    String settings,
    int sortOrder,
    List<OrderFormFieldOptionResponse> options) {

  public OrderFormFieldResponse {
    options = List.copyOf(options);
  }

  @Override
  public List<OrderFormFieldOptionResponse> options() {
    return List.copyOf(options);
  }

  public static OrderFormFieldResponse from(OrderFormFieldResult result) {
    return new OrderFormFieldResponse(
        result.id(),
        result.groupId(),
        result.label(),
        result.fieldType(),
        result.required(),
        result.settings(),
        result.sortOrder(),
        result.options().stream().map(OrderFormFieldOptionResponse::from).toList());
  }
}
