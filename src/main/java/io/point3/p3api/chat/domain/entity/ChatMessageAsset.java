package io.point3.p3api.chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "chat_message_assets",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_chat_message_assets_message_sort_order",
            columnNames = {"message_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageAsset {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "message_id", nullable = false)
  private UUID messageId;

  @Column(name = "asset_id", nullable = false)
  private UUID assetId;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  private ChatMessageAsset(UUID messageId, UUID assetId, int sortOrder) {
    this.messageId = messageId;
    this.assetId = assetId;
    this.sortOrder = sortOrder;
  }

  public static ChatMessageAsset create(UUID messageId, UUID assetId, int sortOrder) {
    Objects.requireNonNull(messageId, "messageId");
    Objects.requireNonNull(assetId, "assetId");

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new ChatMessageAsset(messageId, assetId, sortOrder);
  }
}
