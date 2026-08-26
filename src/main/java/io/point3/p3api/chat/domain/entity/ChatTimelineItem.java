package io.point3.p3api.chat.domain.entity;

import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "chat_timeline_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatTimelineItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "inquiry_id", nullable = false)
  private UUID inquiryId;

  @Column(name = "sender_user_id")
  private UUID senderUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private ChatTimelineItemType type;

  @Column(name = "reference_id", nullable = false)
  private UUID referenceId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private ChatTimelineItem(
      UUID inquiryId, UUID senderUserId, ChatTimelineItemType type, UUID referenceId) {
    this.inquiryId = inquiryId;
    this.senderUserId = senderUserId;
    this.type = type;
    this.referenceId = referenceId;
  }

  public static ChatTimelineItem message(UUID inquiryId, UUID senderUserId, UUID chatMessageId) {
    return create(inquiryId, senderUserId, ChatTimelineItemType.MESSAGE, chatMessageId);
  }

  public static ChatTimelineItem orderFormSubmission(
      UUID inquiryId, UUID senderUserId, UUID orderFormSubmissionId) {
    return create(
        inquiryId, senderUserId, ChatTimelineItemType.ORDER_FORM_SUBMISSION, orderFormSubmissionId);
  }

  public static ChatTimelineItem orderConfirmation(
      UUID inquiryId, UUID senderUserId, UUID orderConfirmationId) {
    return create(
        inquiryId, senderUserId, ChatTimelineItemType.ORDER_CONFIRMATION, orderConfirmationId);
  }

  public static ChatTimelineItem orderConfirmationRevisionRequest(
      UUID inquiryId, UUID senderUserId, UUID orderConfirmationId) {
    return create(
        inquiryId,
        senderUserId,
        ChatTimelineItemType.ORDER_CONFIRMATION_REVISION,
        orderConfirmationId);
  }

  private static ChatTimelineItem create(
      UUID inquiryId, UUID senderUserId, ChatTimelineItemType type, UUID referenceId) {
    Objects.requireNonNull(inquiryId, "inquiryId");
    Objects.requireNonNull(senderUserId, "senderUserId");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(referenceId, "referenceId");

    return new ChatTimelineItem(inquiryId, senderUserId, type, referenceId);
  }
}
