package io.point3.p3api.orderform.controller.response;

import io.point3.p3api.orderform.application.result.OrderFormResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderFormResponse(
    UUID id,
    UUID storeId,
    String name,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    List<OrderFormFieldResponse> fields) {

  public OrderFormResponse {
    fields = List.copyOf(fields);
  }

  @Override
  public List<OrderFormFieldResponse> fields() {
    return List.copyOf(fields);
  }

  public static OrderFormResponse from(OrderFormResult result) {
    return new OrderFormResponse(
        result.id(),
        result.storeId(),
        result.name(),
        result.active(),
        result.createdAt(),
        result.updatedAt(),
        result.fields().stream().map(OrderFormFieldResponse::from).toList());
  }
}
