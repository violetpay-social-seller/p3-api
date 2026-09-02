package io.point3.p3api.orderform.domain.entity;

import io.point3.p3api.orderform.domain.type.OptionInputType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "order_form_options",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_order_form_options_group_sort_order",
            columnNames = {"option_group_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFormOption {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "option_group_id", nullable = false)
  private UUID optionGroupId;

  @Column(name = "label", nullable = false, length = 100)
  private String label;

  @Column(name = "value", nullable = false, length = 100)
  private String value;

  @Enumerated(EnumType.STRING)
  @Column(name = "input_type", nullable = false, length = 30)
  private OptionInputType inputType;

  @Column(name = "price")
  private Long price;

  @Column(name = "price_label", length = 100)
  private String priceLabel;

  @Column(name = "settings", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String settings;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(name = "active", nullable = false)
  private boolean active;

  private OrderFormOption(
      UUID optionGroupId,
      String label,
      String value,
      OptionInputType inputType,
      Long price,
      String priceLabel,
      String settings,
      int sortOrder) {
    this.optionGroupId = optionGroupId;
    this.label = label;
    this.value = value;
    this.inputType = inputType;
    this.price = price;
    this.priceLabel = priceLabel;
    this.settings = settings;
    this.sortOrder = sortOrder;
    this.active = true;
  }

  public static OrderFormOption create(
      UUID optionGroupId,
      String label,
      String value,
      OptionInputType inputType,
      Long price,
      String priceLabel,
      String settings,
      int sortOrder) {
    Objects.requireNonNull(optionGroupId, "optionGroupId");
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(value, "value");
    Objects.requireNonNull(inputType, "inputType");

    if (price != null && price < 0) {
      throw new IllegalArgumentException("price must be greater than or equal to 0");
    }
    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new OrderFormOption(
        optionGroupId, label, value, inputType, price, priceLabel, settings, sortOrder);
  }

  public static OrderFormOption create(
      UUID optionGroupId,
      String label,
      String value,
      OptionInputType inputType,
      Long price,
      String settings,
      int sortOrder) {
    return create(optionGroupId, label, value, inputType, price, null, settings, sortOrder);
  }

  public void inactive() {
    this.active = false;
  }
}
