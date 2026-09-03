package io.point3.p3api.store.domain.entity;

import io.point3.p3api.store.domain.type.StoreNoticeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "store_notices",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_store_notices_store_type",
            columnNames = {"store_id", "type"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreNotice {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private StoreNoticeType type;

  @Column(name = "content", nullable = false, columnDefinition = "text")
  private String content;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private StoreNotice(UUID storeId, StoreNoticeType type, String content) {
    this.storeId = storeId;
    this.type = type;
    this.content = content;
  }

  public static StoreNotice create(UUID storeId, StoreNoticeType type, String content) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(content, "content");

    return new StoreNotice(storeId, type, content);
  }
}
