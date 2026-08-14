package io.point3.p3api.product.application.result;

import io.point3.p3api.product.domain.entity.Product;
import io.point3.p3api.product.domain.type.ProductStatus;
import java.time.Instant;
import java.util.UUID;

public record ProductResult(
    UUID id,
    UUID storeId,
    String name,
    String description,
    Long basePrice,
    ProductStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static ProductResult from(Product product) {
    return new ProductResult(
        product.getId(),
        product.getStoreId(),
        product.getName(),
        product.getDescription(),
        product.getBasePrice(),
        product.getStatus(),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }
}
