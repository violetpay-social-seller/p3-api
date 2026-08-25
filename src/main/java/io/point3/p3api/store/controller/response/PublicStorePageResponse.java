package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.publicquery.result.PublicStorePage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicStorePageResponse(
    List<PublicStoreResponse> items,
    boolean hasNext,
    Instant nextCursorUpdatedAt,
    UUID nextCursorId) {

  public static PublicStorePageResponse from(PublicStorePage page) {
    return new PublicStorePageResponse(
        page.items().stream().map(PublicStoreResponse::from).toList(),
        page.hasNext(),
        page.nextCursorUpdatedAt(),
        page.nextCursorId());
  }
}
