package io.point3.p3api.inquiry.application.result;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.store.domain.entity.Store;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InquiryChatDetail(
    UUID inquiryId,
    UUID storeId,
    String storeName,
    String storeSlug,
    Participant participant,
    List<OrderStartReferenceAssetResult> startReferenceAssets,
    Instant myLastReadAt,
    Instant participantLastReadAt,
    Instant createdAt) {

  public InquiryChatDetail {
    startReferenceAssets = List.copyOf(startReferenceAssets);
  }

  public static InquiryChatDetail of(
      Inquiry inquiry,
      Store store,
      Participant participant,
      List<OrderStartReferenceAssetResult> startReferenceAssets,
      Instant myLastReadAt,
      Instant participantLastReadAt) {
    return new InquiryChatDetail(
        inquiry.getId(),
        store.getId(),
        store.getName(),
        store.getSlug(),
        participant,
        startReferenceAssets,
        myLastReadAt,
        participantLastReadAt,
        inquiry.getCreatedAt());
  }

  @Override
  public List<OrderStartReferenceAssetResult> startReferenceAssets() {
    return List.copyOf(startReferenceAssets);
  }

  public record Participant(UUID userId, String name) {}
}
