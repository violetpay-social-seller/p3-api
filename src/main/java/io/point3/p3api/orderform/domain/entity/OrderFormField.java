package io.point3.p3api.orderform.domain.entity;

import io.point3.p3api.orderform.domain.type.FieldType;
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
    name = "order_form_fields",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_order_form_fields_group_sort_order",
            columnNames = {"group_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFormField {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "group_id", nullable = false)
  private UUID groupId;

  @Column(name = "label", nullable = false, length = 150)
  private String label;

  @Enumerated(EnumType.STRING)
  @Column(name = "field_type", nullable = false, length = 30)
  private FieldType fieldType;

  @Column(name = "required", nullable = false)
  private boolean required;

  @Column(name = "settings", columnDefinition = "jsonb")
  private String settings;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  private OrderFormField(
      UUID groupId,
      String label,
      FieldType fieldType,
      boolean required,
      String settings,
      int sortOrder) {
    this.groupId = groupId;
    this.label = label;
    this.fieldType = fieldType;
    this.required = required;
    this.settings = settings;
    this.sortOrder = sortOrder;
  }

  public static OrderFormField create(
      UUID groupId,
      String label,
      FieldType fieldType,
      boolean required,
      String settings,
      int sortOrder) {
    Objects.requireNonNull(groupId, "groupId");
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(fieldType, "fieldType");

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new OrderFormField(groupId, label, fieldType, required, settings, sortOrder);
  }
}
