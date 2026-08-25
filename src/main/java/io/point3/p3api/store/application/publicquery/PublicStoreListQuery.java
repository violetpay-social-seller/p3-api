package io.point3.p3api.store.application.publicquery;

import java.time.Instant;
import java.util.UUID;

public record PublicStoreListQuery(Instant cursorUpdatedAt, UUID cursorId, Integer size) {}
