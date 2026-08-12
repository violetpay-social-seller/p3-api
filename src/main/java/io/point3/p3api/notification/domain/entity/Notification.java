package io.point3.p3api.notification.domain.entity;

import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
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
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "reference_type", length = 50)
  private NotificationReferenceType referenceType;

  @Column(name = "reference_id")
  private UUID referenceId;

  @Column(name = "title", nullable = false, length = 150)
  private String title;

  @Column(name = "body", nullable = false, columnDefinition = "text")
  private String body;

  @Column(name = "read_at")
  private Instant readAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private Notification(
      UUID userId,
      NotificationType type,
      NotificationReferenceType referenceType,
      UUID referenceId,
      String title,
      String body) {
    this.userId = userId;
    this.type = type;
    this.referenceType = referenceType;
    this.referenceId = referenceId;
    this.title = title;
    this.body = body;
  }

  public static Notification create(
      UUID userId,
      NotificationType type,
      NotificationReferenceType referenceType,
      UUID referenceId,
      String title,
      String body) {
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(body, "body");

    return new Notification(userId, type, referenceType, referenceId, title, body);
  }

  public void read(Instant readAt) {
    Objects.requireNonNull(readAt, "readAt");
    this.readAt = readAt;
  }
}
