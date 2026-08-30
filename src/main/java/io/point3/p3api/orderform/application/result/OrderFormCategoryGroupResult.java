package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormCategoryGroup;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import java.util.List;
import java.util.UUID;

public record OrderFormCategoryGroupResult(
    UUID id,
    OrderFormCategory category,
    String title,
    String description,
    int sortOrder,
    List<OrderFormOptionGroupResult> optionGroups) {

  public OrderFormCategoryGroupResult {
    optionGroups = List.copyOf(optionGroups);
  }

  public static OrderFormCategoryGroupResult of(
      OrderFormCategoryGroup group, List<OrderFormOptionGroupResult> optionGroups) {
    return new OrderFormCategoryGroupResult(
        group.getId(),
        group.getCategory(),
        group.getTitle(),
        group.getDescription(),
        group.getSortOrder(),
        optionGroups);
  }
}
