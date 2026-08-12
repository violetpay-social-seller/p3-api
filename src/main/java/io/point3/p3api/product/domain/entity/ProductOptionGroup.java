package io.point3.p3api.product.domain.entity;

import io.point3.p3api.product.domain.type.SelectionType;
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
    name = "product_option_groups",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_product_option_groups_product_sort_order",
            columnNames = {"product_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "selection_type", nullable = false, length = 30)
  private SelectionType selectionType;

  @Column(name = "required", nullable = false)
  private boolean required;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  private ProductOptionGroup(
      UUID productId, String name, SelectionType selectionType, boolean required, int sortOrder) {
    this.productId = productId;
    this.name = name;
    this.selectionType = selectionType;
    this.required = required;
    this.sortOrder = sortOrder;
  }

  public static ProductOptionGroup create(
      UUID productId, String name, SelectionType selectionType, boolean required, int sortOrder) {
    Objects.requireNonNull(productId, "productId");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(selectionType, "selectionType");

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new ProductOptionGroup(productId, name, selectionType, required, sortOrder);
  }
}
