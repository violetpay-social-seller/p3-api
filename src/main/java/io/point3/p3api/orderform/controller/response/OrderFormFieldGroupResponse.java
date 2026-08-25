package io.point3.p3api.orderform.controller.response;

import io.point3.p3api.orderform.application.result.OrderFormFieldGroupResult;
import java.util.List;
import java.util.UUID;

public record OrderFormFieldGroupResponse(
    UUID id, String title, String description, int sortOrder, List<OrderFormFieldResponse> fields) {

  public OrderFormFieldGroupResponse {
    fields = List.copyOf(fields);
  }

  @Override
  public List<OrderFormFieldResponse> fields() {
    return List.copyOf(fields);
  }

  public static OrderFormFieldGroupResponse from(OrderFormFieldGroupResult result) {
    return new OrderFormFieldGroupResponse(
        result.id(),
        result.title(),
        result.description(),
        result.sortOrder(),
        result.fields().stream().map(OrderFormFieldResponse::from).toList());
  }
}
