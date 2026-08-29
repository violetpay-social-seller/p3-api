package io.point3.p3api.orderform.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "order_form_field_options",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_order_form_field_options_field_sort_order",
            columnNames = {"field_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFormFieldOption {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "field_id", nullable = false)
  private UUID fieldId;

  @Column(name = "label", nullable = false, length = 100)
  private String label;

  @Column(name = "value", nullable = false, length = 100)
  private String value;

  @Column(name = "price", nullable = false)
  private long price;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(name = "active", nullable = false)
  private boolean active;

  private OrderFormFieldOption(
      UUID fieldId, String label, String value, long price, int sortOrder) {
    this.fieldId = fieldId;
    this.label = label;
    this.value = value;
    this.price = price;
    this.sortOrder = sortOrder;
    this.active = true;
  }

  public static OrderFormFieldOption create(
      UUID fieldId, String label, String value, long price, int sortOrder) {
    Objects.requireNonNull(fieldId, "fieldId");
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(value, "value");

    if (price < 0) {
      throw new IllegalArgumentException("price must be greater than or equal to 0");
    }
    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new OrderFormFieldOption(fieldId, label, value, price, sortOrder);
  }

  public void inactive() {
    this.active = false;
  }
}
