package io.point3.p3api.chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "inquiry_id", nullable = false)
  private UUID inquiryId;

  @Column(name = "sender_user_id", nullable = false)
  private UUID senderUserId;

  @Column(name = "content", columnDefinition = "text")
  private String content;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private ChatMessage(UUID inquiryId, UUID senderUserId, String content) {
    this.inquiryId = inquiryId;
    this.senderUserId = senderUserId;
    this.content = content;
  }

  public static ChatMessage create(UUID inquiryId, UUID senderUserId, String content) {
    Objects.requireNonNull(inquiryId, "inquiryId");
    Objects.requireNonNull(senderUserId, "senderUserId");

    if (content != null && content.isBlank()) {
      // TODO: Chat 도메인 예외로 변경 필요
      throw new IllegalArgumentException("content must not be blank");
    }

    return new ChatMessage(inquiryId, senderUserId, content);
  }
}
