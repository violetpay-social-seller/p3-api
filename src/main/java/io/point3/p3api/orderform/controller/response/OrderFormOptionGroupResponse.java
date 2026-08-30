package io.point3.p3api.orderform.controller.response;

import io.point3.p3api.orderform.application.result.OrderFormOptionGroupResult;
import io.point3.p3api.orderform.domain.type.SelectionType;
import java.util.List;
import java.util.UUID;

public record OrderFormOptionGroupResponse(
    UUID id,
    UUID categoryGroupId,
    String label,
    SelectionType selectionType,
    boolean required,
    int sortOrder,
    List<OrderFormOptionResponse> options) {

  public OrderFormOptionGroupResponse {
    options = List.copyOf(options);
  }

  @Override
  public List<OrderFormOptionResponse> options() {
    return List.copyOf(options);
  }

  public static OrderFormOptionGroupResponse from(OrderFormOptionGroupResult result) {
    return new OrderFormOptionGroupResponse(
        result.id(),
        result.categoryGroupId(),
        result.label(),
        result.selectionType(),
        result.required(),
        result.sortOrder(),
        result.options().stream().map(OrderFormOptionResponse::from).toList());
  }
}
