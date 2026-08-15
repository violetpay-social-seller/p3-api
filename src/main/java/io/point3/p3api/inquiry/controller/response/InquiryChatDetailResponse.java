package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import java.time.Instant;
import java.util.UUID;

public record InquiryChatDetailResponse(
    UUID inquiryId,
    UUID storeId,
    String storeName,
    String storeSlug,
    ParticipantResponse participant,
    ProductContextResponse product,
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
        ProductContextResponse.from(detail.product()),
        detail.myLastReadAt(),
        detail.participantLastReadAt(),
        detail.createdAt());
  }

  public record ParticipantResponse(UUID userId, String name) {

    private static ParticipantResponse from(InquiryChatDetail.Participant participant) {
      return new ParticipantResponse(participant.userId(), participant.name());
    }
  }

  public record ProductContextResponse(UUID productId, String name) {

    private static ProductContextResponse from(InquiryChatDetail.ProductContext product) {
      if (product == null) {
        return null;
      }

      return new ProductContextResponse(product.productId(), product.name());
    }
  }
}
