package io.point3.p3api.product.domain.entity;

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
    name = "product_options",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_product_options_group_sort_order",
            columnNames = {"option_group_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "option_group_id", nullable = false)
  private UUID optionGroupId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "additional_price", nullable = false)
  private long additionalPrice;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(name = "active", nullable = false)
  private boolean active;

  private ProductOption(UUID optionGroupId, String name, long additionalPrice, int sortOrder) {
    this.optionGroupId = optionGroupId;
    this.name = name;
    this.additionalPrice = additionalPrice;
    this.sortOrder = sortOrder;
    this.active = true;
  }

  public static ProductOption create(
      UUID optionGroupId, String name, long additionalPrice, int sortOrder) {
    Objects.requireNonNull(optionGroupId, "optionGroupId");
    Objects.requireNonNull(name, "name");

    if (additionalPrice < 0) {
      throw new IllegalArgumentException("additionalPrice must be greater than or equal to 0");
    }

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new ProductOption(optionGroupId, name, additionalPrice, sortOrder);
  }

  public void inactive() {
    this.active = false;
  }
}
