package io.point3.p3api.order.domain.entity;

import io.point3.p3api.order.domain.type.OrderStatus;
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

@Entity
@Table(name = "order_status_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "previous_status", length = 30)
  private OrderStatus previousStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", nullable = false, length = 30)
  private OrderStatus newStatus;

  @Column(name = "changed_by")
  private UUID changedBy;

  @Column(name = "reason", columnDefinition = "text")
  private String reason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private OrderStatusHistory(
      UUID orderId,
      OrderStatus previousStatus,
      OrderStatus newStatus,
      UUID changedBy,
      String reason,
      Instant createdAt) {
    this.orderId = orderId;
    this.previousStatus = previousStatus;
    this.newStatus = newStatus;
    this.changedBy = changedBy;
    this.reason = reason;
    this.createdAt = createdAt;
  }

  public static OrderStatusHistory create(
      UUID orderId,
      OrderStatus previousStatus,
      OrderStatus newStatus,
      UUID changedBy,
      String reason,
      Instant createdAt) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(newStatus, "newStatus");
    Objects.requireNonNull(createdAt, "createdAt");

    return new OrderStatusHistory(orderId, previousStatus, newStatus, changedBy, reason, createdAt);
  }
}
