package io.point3.p3api.product.controller.response;

import io.point3.p3api.product.application.result.ProductResult;
import io.point3.p3api.product.domain.type.ProductStatus;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    UUID storeId,
    String name,
    String description,
    Long basePrice,
    ProductStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static ProductResponse from(ProductResult result) {
    return new ProductResponse(
        result.id(),
        result.storeId(),
        result.name(),
        result.description(),
        result.basePrice(),
        result.status(),
        result.createdAt(),
        result.updatedAt());
  }
}
