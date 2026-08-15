package io.point3.p3api.inquiry.application.result;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.store.domain.entity.Store;
import java.time.Instant;
import java.util.UUID;

public record InquiryChatDetail(
    UUID inquiryId,
    UUID storeId,
    String storeName,
    String storeSlug,
    Participant participant,
    ProductContext product,
    Instant myLastReadAt,
    Instant participantLastReadAt,
    Instant createdAt) {

  public static InquiryChatDetail of(
      Inquiry inquiry,
      Store store,
      Participant participant,
      ProductContext product,
      Instant myLastReadAt,
      Instant participantLastReadAt) {
    return new InquiryChatDetail(
        inquiry.getId(),
        store.getId(),
        store.getName(),
        store.getSlug(),
        participant,
        product,
        myLastReadAt,
        participantLastReadAt,
        inquiry.getCreatedAt());
  }

  public record Participant(UUID userId, String name) {}

  public record ProductContext(UUID productId, String name) {}
}
