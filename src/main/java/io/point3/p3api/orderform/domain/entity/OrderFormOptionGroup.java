package io.point3.p3api.orderform.domain.entity;

import io.point3.p3api.orderform.domain.type.SelectionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "order_form_option_groups",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_order_form_option_groups_category_group_sort_order",
            columnNames = {"category_group_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFormOptionGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "category_group_id", nullable = false)
  private UUID categoryGroupId;

  @Column(name = "label", nullable = false, length = 150)
  private String label;

  @Enumerated(EnumType.STRING)
  @Column(name = "selection_type", nullable = false, length = 30)
  private SelectionType selectionType;

  @Column(name = "required", nullable = false)
  private boolean required;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  private OrderFormOptionGroup(
      UUID categoryGroupId,
      String label,
      SelectionType selectionType,
      boolean required,
      int sortOrder) {
    this.categoryGroupId = categoryGroupId;
    this.label = label;
    this.selectionType = selectionType;
    this.required = required;
    this.sortOrder = sortOrder;
  }

  public static OrderFormOptionGroup create(
      UUID categoryGroupId,
      String label,
      SelectionType selectionType,
      boolean required,
      int sortOrder) {
    Objects.requireNonNull(categoryGroupId, "categoryGroupId");
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(selectionType, "selectionType");

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new OrderFormOptionGroup(categoryGroupId, label, selectionType, required, sortOrder);
  }
}
