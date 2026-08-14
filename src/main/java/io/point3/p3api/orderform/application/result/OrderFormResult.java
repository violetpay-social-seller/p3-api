package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormField;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record OrderFormResult(
    UUID id,
    UUID storeId,
    String name,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    List<OrderFormFieldResult> fields) {

  public static OrderFormResult from(OrderFormTemplate template, List<OrderFormField> fields) {
    return new OrderFormResult(
        template.getId(),
        template.getStoreId(),
        template.getName(),
        template.isActive(),
        template.getCreatedAt(),
        template.getUpdatedAt(),
        fields.stream()
            .sorted(Comparator.comparingInt(OrderFormField::getSortOrder))
            .map(OrderFormFieldResult::from)
            .toList());
  }
}
