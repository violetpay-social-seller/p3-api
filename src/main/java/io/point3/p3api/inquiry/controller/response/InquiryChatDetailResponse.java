package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import io.point3.p3api.inquiry.application.result.OrderStartReferenceAssetResult;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InquiryChatDetailResponse(
    UUID inquiryId,
    UUID storeId,
    String storeName,
    String storeSlug,
    ParticipantResponse participant,
    List<StartReferenceAssetResponse> startReferenceAssets,
    Instant myLastReadAt,
    Instant participantLastReadAt,
    Instant createdAt) {

  public InquiryChatDetailResponse {
    startReferenceAssets = List.copyOf(startReferenceAssets);
  }

  public static InquiryChatDetailResponse from(InquiryChatDetail detail) {
    return new InquiryChatDetailResponse(
        detail.inquiryId(),
        detail.storeId(),
        detail.storeName(),
        detail.storeSlug(),
        ParticipantResponse.from(detail.participant()),
        detail.startReferenceAssets().stream()
            .map(StartReferenceAssetResponse::from)
            .toList(),
        detail.myLastReadAt(),
        detail.participantLastReadAt(),
        detail.createdAt());
  }

  @Override
  public List<StartReferenceAssetResponse> startReferenceAssets() {
    return List.copyOf(startReferenceAssets);
  }

  public record ParticipantResponse(UUID userId, String name) {

    private static ParticipantResponse from(InquiryChatDetail.Participant participant) {
      return new ParticipantResponse(participant.userId(), participant.name());
    }
  }

  public record StartReferenceAssetResponse(
      UUID assetId, OrderFormReferenceAssetSource source, int sortOrder) {

    private static StartReferenceAssetResponse from(OrderStartReferenceAssetResult result) {
      return new StartReferenceAssetResponse(result.assetId(), result.source(), result.sortOrder());
    }
  }
}
