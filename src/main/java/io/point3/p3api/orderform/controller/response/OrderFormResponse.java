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
    List<OrderFormOptionGroupResponse> optionGroups,
    List<OrderFormCategoryGroupResponse> groups) {

  public OrderFormResponse {
    optionGroups = List.copyOf(optionGroups);
    groups = List.copyOf(groups);
  }

  @Override
  public List<OrderFormOptionGroupResponse> optionGroups() {
    return List.copyOf(optionGroups);
  }

  @Override
  public List<OrderFormCategoryGroupResponse> groups() {
    return List.copyOf(groups);
  }

  public static OrderFormResponse from(OrderFormResult result) {
    return new OrderFormResponse(
        result.id(),
        result.storeId(),
        result.name(),
        result.active(),
        result.createdAt(),
        result.updatedAt(),
        result.optionGroups().stream().map(OrderFormOptionGroupResponse::from).toList(),
        result.groups().stream().map(OrderFormCategoryGroupResponse::from).toList());
  }
}
