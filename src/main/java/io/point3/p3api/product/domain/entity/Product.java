package io.point3.p3api.product.domain.entity;

import io.point3.p3api.product.domain.type.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "base_price")
  private Long basePrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private ProductStatus status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private Product(UUID storeId, String name, String description, Long basePrice) {
    this.storeId = storeId;
    this.name = name;
    this.description = description;
    this.basePrice = basePrice;
    this.status = ProductStatus.HIDDEN;
  }

  public static Product create(UUID storeId, String name, String description, Long basePrice) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(name, "name");

    if (basePrice != null && basePrice < 0) {
      throw new IllegalArgumentException("basePrice must be greater than or equal to 0");
    }

    return new Product(storeId, name, description, basePrice);
  }

  public void show() {
    this.status = ProductStatus.VISIBLE;
  }

  public void hide() {
    this.status = ProductStatus.HIDDEN;
  }
}
