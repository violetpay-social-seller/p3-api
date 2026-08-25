package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormField;
import io.point3.p3api.orderform.domain.entity.OrderFormFieldGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormFieldOption;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record OrderFormResult(
    UUID id,
    UUID storeId,
    String name,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    List<OrderFormFieldResult> fields,
    List<OrderFormFieldGroupResult> groups) {

  public OrderFormResult {
    fields = List.copyOf(fields);
    groups = List.copyOf(groups);
  }

  @Override
  public List<OrderFormFieldResult> fields() {
    return List.copyOf(fields);
  }

  @Override
  public List<OrderFormFieldGroupResult> groups() {
    return List.copyOf(groups);
  }

  public static OrderFormResult from(
      OrderFormTemplate template,
      List<OrderFormFieldGroup> groups,
      List<OrderFormField> fields,
      List<OrderFormFieldOption> options) {
    Map<UUID, List<OrderFormFieldOptionResult>> optionsByFieldId = options.stream()
        .collect(java.util.stream.Collectors.groupingBy(
            OrderFormFieldOption::getFieldId,
            java.util.stream.Collectors.mapping(
                OrderFormFieldOptionResult::from, java.util.stream.Collectors.toList())));
    Map<UUID, List<OrderFormFieldResult>> fieldsByGroupId = fields.stream()
        .map(field -> OrderFormFieldResult.from(
            field, optionsByFieldId.getOrDefault(field.getId(), List.of())))
        .collect(java.util.stream.Collectors.groupingBy(
            OrderFormFieldResult::groupId, java.util.stream.Collectors.toList()));
    List<OrderFormFieldGroupResult> groupResults = groups.stream()
        .sorted(Comparator.comparingInt(OrderFormFieldGroup::getSortOrder))
        .map(group -> OrderFormFieldGroupResult.of(
            group,
            fieldsByGroupId.getOrDefault(group.getId(), List.of()).stream()
                .sorted(Comparator.comparingInt(OrderFormFieldResult::sortOrder))
                .toList()))
        .toList();
    List<OrderFormFieldResult> fieldResults =
        groupResults.stream().flatMap(group -> group.fields().stream()).toList();
    return new OrderFormResult(
        template.getId(),
        template.getStoreId(),
        template.getName(),
        template.isActive(),
        template.getCreatedAt(),
        template.getUpdatedAt(),
        fieldResults,
        groupResults);
  }
}
