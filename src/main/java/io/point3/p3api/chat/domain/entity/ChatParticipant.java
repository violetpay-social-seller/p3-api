package io.point3.p3api.chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "chat_participants",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_chat_participants_room_user",
            columnNames = {"chat_room_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "chat_room_id", nullable = false)
  private UUID chatRoomId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @CreationTimestamp
  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  private ChatParticipant(UUID chatRoomId, UUID userId) {
    this.chatRoomId = chatRoomId;
    this.userId = userId;
  }

  public static ChatParticipant create(UUID chatRoomId, UUID userId) {
    Objects.requireNonNull(chatRoomId, "chatRoomId");
    Objects.requireNonNull(userId, "userId");

    return new ChatParticipant(chatRoomId, userId);
  }
}
