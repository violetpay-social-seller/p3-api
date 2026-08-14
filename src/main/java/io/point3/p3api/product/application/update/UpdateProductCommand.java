package io.point3.p3api.product.application.update;

import java.util.UUID;

public record UpdateProductCommand(
    UUID storeId, UUID productId, String name, String description, Long basePrice) {}
