package io.point3.p3api.product.application.update;

import io.point3.p3api.product.domain.type.ProductStatus;
import java.util.UUID;

public record ChangeProductStatusCommand(UUID storeId, UUID productId, ProductStatus status) {}
