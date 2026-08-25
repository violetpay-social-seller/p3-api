package io.point3.p3api.store.application.publicquery.result;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicStorePage(
    List<PublicStoreResult> items,
    boolean hasNext,
    Instant nextCursorUpdatedAt,
    UUID nextCursorId) {

  public PublicStorePage {
    items = List.copyOf(items);
  }

  @Override
  public List<PublicStoreResult> items() {
    return List.copyOf(items);
  }
}
