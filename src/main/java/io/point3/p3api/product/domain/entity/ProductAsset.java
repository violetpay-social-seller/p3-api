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
    name = "product_assets",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_product_assets_product_sort_order",
            columnNames = {"product_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductAsset {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "asset_id", nullable = false)
  private UUID assetId;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(name = "is_primary", nullable = false)
  private boolean primary;

  private ProductAsset(UUID productId, UUID assetId, int sortOrder, boolean primary) {
    this.productId = productId;
    this.assetId = assetId;
    this.sortOrder = sortOrder;
    this.primary = primary;
  }

  public static ProductAsset create(UUID productId, UUID assetId, int sortOrder, boolean primary) {
    Objects.requireNonNull(productId, "productId");
    Objects.requireNonNull(assetId, "assetId");

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new ProductAsset(productId, assetId, sortOrder, primary);
  }
}
