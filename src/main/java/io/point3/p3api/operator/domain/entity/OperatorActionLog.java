package io.point3.p3api.operator.domain.entity;

import io.point3.p3api.operator.domain.type.OperatorActionType;
import io.point3.p3api.operator.domain.type.OperatorTargetType;
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
@Table(name = "operator_action_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperatorActionLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "operator_user_id", nullable = false)
  private UUID operatorUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false, length = 60)
  private OperatorActionType actionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 60)
  private OperatorTargetType targetType;

  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Column(name = "reason", columnDefinition = "text")
  private String reason;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private OperatorActionLog(
      UUID operatorUserId,
      OperatorActionType actionType,
      OperatorTargetType targetType,
      UUID targetId,
      String reason) {
    this.operatorUserId = operatorUserId;
    this.actionType = actionType;
    this.targetType = targetType;
    this.targetId = targetId;
    this.reason = reason;
  }

  public static OperatorActionLog create(
      UUID operatorUserId,
      OperatorActionType actionType,
      OperatorTargetType targetType,
      UUID targetId,
      String reason) {
    Objects.requireNonNull(operatorUserId, "operatorUserId");
    Objects.requireNonNull(actionType, "actionType");
    Objects.requireNonNull(targetType, "targetType");
    Objects.requireNonNull(targetId, "targetId");

    return new OperatorActionLog(operatorUserId, actionType, targetType, targetId, reason);
  }
}
