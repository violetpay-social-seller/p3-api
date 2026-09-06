package io.point3.p3api.store.application.update;

import java.util.UUID;

public record UpdateStoreDescriptionCommand(UUID storeId, String description) {}
