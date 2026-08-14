package io.point3.p3api.chat.domain.entity;

import io.point3.p3api.chat.domain.type.ChatEventType;
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
@Table(name = "chat_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "inquiry_id", nullable = false)
  private UUID inquiryId;

  @Column(name = "sender_user_id")
  private UUID senderUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private ChatEventType type;

  @Column(name = "reference_id", nullable = false)
  private UUID referenceId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private ChatEvent(UUID inquiryId, UUID senderUserId, ChatEventType type, UUID referenceId) {
    this.inquiryId = inquiryId;
    this.senderUserId = senderUserId;
    this.type = type;
    this.referenceId = referenceId;
  }

  public static ChatEvent message(UUID inquiryId, UUID senderUserId, UUID chatMessageId) {
    return create(inquiryId, senderUserId, ChatEventType.MESSAGE, chatMessageId);
  }

  public static ChatEvent orderFormSubmission(
      UUID inquiryId, UUID senderUserId, UUID orderFormSubmissionId) {
    return create(
        inquiryId, senderUserId, ChatEventType.ORDER_FORM_SUBMISSION, orderFormSubmissionId);
  }

  public static ChatEvent orderConfirmation(
      UUID inquiryId, UUID senderUserId, UUID orderConfirmationId) {
    return create(inquiryId, senderUserId, ChatEventType.ORDER_CONFIRMATION, orderConfirmationId);
  }

  public static ChatEvent paymentRequest(UUID inquiryId, UUID senderUserId, UUID paymentRequestId) {
    return create(inquiryId, senderUserId, ChatEventType.PAYMENT_REQUEST, paymentRequestId);
  }

  private static ChatEvent create(
      UUID inquiryId, UUID senderUserId, ChatEventType type, UUID referenceId) {
    Objects.requireNonNull(inquiryId, "inquiryId");
    Objects.requireNonNull(senderUserId, "senderUserId");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(referenceId, "referenceId");

    return new ChatEvent(inquiryId, senderUserId, type, referenceId);
  }
}
