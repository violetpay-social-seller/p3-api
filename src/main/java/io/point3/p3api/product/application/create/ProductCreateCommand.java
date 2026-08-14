package io.point3.p3api.product.application.create;

import java.util.UUID;

public record ProductCreateCommand(UUID storeId, String name, String description, Long basePrice) {}
