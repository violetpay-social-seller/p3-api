package io.point3.p3api.store.application.update;

import io.point3.p3api.store.domain.type.StoreStatus;
import java.util.UUID;

public record ChangeStoreStatusCommand(UUID ownerUserId, StoreStatus status) {}
