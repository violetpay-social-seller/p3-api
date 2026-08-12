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

  @Column(name = "chat_room_id", nullable = false)
  private UUID chatRoomId;

  @Column(name = "sender_id", nullable = false)
  private UUID senderId;

  @Column(name = "content", nullable = false, columnDefinition = "text")
  private String content;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private ChatMessage(UUID chatRoomId, UUID senderId, String content) {
    this.chatRoomId = chatRoomId;
    this.senderId = senderId;
    this.content = content;
  }

  public static ChatMessage create(UUID chatRoomId, UUID senderId, String content) {
    Objects.requireNonNull(chatRoomId, "chatRoomId");
    Objects.requireNonNull(senderId, "senderId");
    Objects.requireNonNull(content, "content");

    if (content.isBlank()) {
      // TODO: Chat 도메인 예외로 변경 필요
      throw new IllegalArgumentException("content must not be blank");
    }

    return new ChatMessage(chatRoomId, senderId, content);
  }
}
