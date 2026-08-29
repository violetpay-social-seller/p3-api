package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormField;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.List;
import java.util.UUID;

public record OrderFormFieldResult(
    UUID id,
    UUID groupId,
    String label,
    FieldType fieldType,
    boolean required,
    Long price,
    String settings,
    int sortOrder,
    List<OrderFormFieldOptionResult> options) {

  public OrderFormFieldResult {
    options = List.copyOf(options);
  }

  public OrderFormFieldResult(
      UUID id,
      UUID groupId,
      String label,
      FieldType fieldType,
      boolean required,
      Long price,
      String settings,
      int sortOrder) {
    this(id, groupId, label, fieldType, required, price, settings, sortOrder, List.of());
  }

  public static OrderFormFieldResult from(
      OrderFormField field, List<OrderFormFieldOptionResult> options) {
    return new OrderFormFieldResult(
        field.getId(),
        field.getGroupId(),
        field.getLabel(),
        field.getFieldType(),
        field.isRequired(),
        field.getPrice(),
        field.getSettings(),
        field.getSortOrder(),
        options);
  }
}
