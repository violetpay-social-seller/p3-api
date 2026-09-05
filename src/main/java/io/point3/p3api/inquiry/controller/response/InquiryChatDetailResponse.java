package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import io.point3.p3api.inquiry.application.result.OrderStartReferenceAssetResult;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.time.Instant;
import java.util.UUID;

public record InquiryChatDetailResponse(
    UUID inquiryId,
    UUID storeId,
    String storeName,
    String storeSlug,
    ParticipantResponse participant,
    StartReferenceAssetResponse startReferenceAsset,
    Instant myLastReadAt,
    Instant participantLastReadAt,
    Instant createdAt) {

  public static InquiryChatDetailResponse from(InquiryChatDetail detail) {
    return new InquiryChatDetailResponse(
        detail.inquiryId(),
        detail.storeId(),
        detail.storeName(),
        detail.storeSlug(),
        ParticipantResponse.from(detail.participant()),
        StartReferenceAssetResponse.from(detail.startReferenceAsset()),
        detail.myLastReadAt(),
        detail.participantLastReadAt(),
        detail.createdAt());
  }

  public record ParticipantResponse(UUID userId, String name) {

    private static ParticipantResponse from(InquiryChatDetail.Participant participant) {
      return new ParticipantResponse(participant.userId(), participant.name());
    }
  }

  public record StartReferenceAssetResponse(
      UUID assetId, OrderFormReferenceAssetSource source, String deliveryUrl) {

    private static StartReferenceAssetResponse from(OrderStartReferenceAssetResult result) {
      if (result == null) {
        return null;
      }
      return new StartReferenceAssetResponse(
          result.assetId(), result.source(), result.deliveryUrl());
    }
  }
}
