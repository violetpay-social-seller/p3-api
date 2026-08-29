package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormFieldGroup;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import java.util.List;
import java.util.UUID;

public record OrderFormFieldGroupResult(
    UUID id,
    OrderFormCategory category,
    String title,
    String description,
    int sortOrder,
    List<OrderFormFieldResult> fields) {

  public OrderFormFieldGroupResult {
    fields = List.copyOf(fields);
  }

  public static OrderFormFieldGroupResult of(
      OrderFormFieldGroup group, List<OrderFormFieldResult> fields) {
    return new OrderFormFieldGroupResult(
        group.getId(),
        group.getCategory(),
        group.getTitle(),
        group.getDescription(),
        group.getSortOrder(),
        fields);
  }
}
