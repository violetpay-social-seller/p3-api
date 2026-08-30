package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormCategoryGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormOption;
import io.point3.p3api.orderform.domain.entity.OrderFormOptionGroup;
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
    List<OrderFormOptionGroupResult> optionGroups,
    List<OrderFormCategoryGroupResult> groups) {

  public OrderFormResult {
    optionGroups = List.copyOf(optionGroups);
    groups = List.copyOf(groups);
  }

  @Override
  public List<OrderFormOptionGroupResult> optionGroups() {
    return List.copyOf(optionGroups);
  }

  @Override
  public List<OrderFormCategoryGroupResult> groups() {
    return List.copyOf(groups);
  }

  public static OrderFormResult from(
      OrderFormTemplate template,
      List<OrderFormCategoryGroup> groups,
      List<OrderFormOptionGroup> optionGroups,
      List<OrderFormOption> options) {
    Map<UUID, List<OrderFormOptionResult>> optionsByOptionGroupId = options.stream()
        .collect(java.util.stream.Collectors.groupingBy(
            OrderFormOption::getOptionGroupId,
            java.util.stream.Collectors.mapping(
                OrderFormOptionResult::from, java.util.stream.Collectors.toList())));
    Map<UUID, List<OrderFormOptionGroupResult>> optionGroupsByCategoryGroupId =
        optionGroups.stream()
            .map(optionGroup -> OrderFormOptionGroupResult.from(
                optionGroup, optionsByOptionGroupId.getOrDefault(optionGroup.getId(), List.of())))
            .collect(java.util.stream.Collectors.groupingBy(
                OrderFormOptionGroupResult::categoryGroupId, java.util.stream.Collectors.toList()));
    List<OrderFormCategoryGroupResult> categoryGroupResults = groups.stream()
        .sorted(Comparator.comparingInt(OrderFormCategoryGroup::getSortOrder))
        .map(group -> OrderFormCategoryGroupResult.of(
            group,
            optionGroupsByCategoryGroupId.getOrDefault(group.getId(), List.of()).stream()
                .sorted(Comparator.comparingInt(OrderFormOptionGroupResult::sortOrder))
                .toList()))
        .toList();
    List<OrderFormOptionGroupResult> optionGroupResults = categoryGroupResults.stream()
        .flatMap(group -> group.optionGroups().stream())
        .toList();
    return new OrderFormResult(
        template.getId(),
        template.getStoreId(),
        template.getName(),
        template.isActive(),
        template.getCreatedAt(),
        template.getUpdatedAt(),
        optionGroupResults,
        categoryGroupResults);
  }
}
