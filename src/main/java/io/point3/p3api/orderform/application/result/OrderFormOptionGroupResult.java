package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormOptionGroup;
import io.point3.p3api.orderform.domain.type.SelectionType;
import java.util.List;
import java.util.UUID;

public record OrderFormOptionGroupResult(
    UUID id,
    UUID categoryGroupId,
    String label,
    SelectionType selectionType,
    boolean required,
    int sortOrder,
    List<OrderFormOptionResult> options) {

  public OrderFormOptionGroupResult {
    options = List.copyOf(options);
  }

  public OrderFormOptionGroupResult(
      UUID id,
      UUID categoryGroupId,
      String label,
      SelectionType selectionType,
      boolean required,
      int sortOrder) {
    this(id, categoryGroupId, label, selectionType, required, sortOrder, List.of());
  }

  public static OrderFormOptionGroupResult from(
      OrderFormOptionGroup optionGroup, List<OrderFormOptionResult> options) {
    return new OrderFormOptionGroupResult(
        optionGroup.getId(),
        optionGroup.getCategoryGroupId(),
        optionGroup.getLabel(),
        optionGroup.getSelectionType(),
        optionGroup.isRequired(),
        optionGroup.getSortOrder(),
        options);
  }
}
