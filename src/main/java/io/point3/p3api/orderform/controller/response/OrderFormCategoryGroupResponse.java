package io.point3.p3api.orderform.controller.response;

import io.point3.p3api.orderform.application.result.OrderFormCategoryGroupResult;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import java.util.List;
import java.util.UUID;

public record OrderFormCategoryGroupResponse(
    UUID id,
    OrderFormCategory category,
    String title,
    String description,
    int sortOrder,
    List<OrderFormOptionGroupResponse> optionGroups) {

  public OrderFormCategoryGroupResponse {
    optionGroups = List.copyOf(optionGroups);
  }

  @Override
  public List<OrderFormOptionGroupResponse> optionGroups() {
    return List.copyOf(optionGroups);
  }

  public static OrderFormCategoryGroupResponse from(OrderFormCategoryGroupResult result) {
    return new OrderFormCategoryGroupResponse(
        result.id(),
        result.category(),
        result.title(),
        result.description(),
        result.sortOrder(),
        result.optionGroups().stream().map(OrderFormOptionGroupResponse::from).toList());
  }
}
