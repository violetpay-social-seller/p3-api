package io.point3.p3api.operator.domain.entity;

import io.point3.p3api.operator.domain.type.ReportStatus;
import io.point3.p3api.operator.domain.type.ReportTargetType;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "reporter_user_id", nullable = false)
  private UUID reporterUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 60)
  private ReportTargetType targetType;

  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Column(name = "reason", nullable = false, columnDefinition = "text")
  private String reason;

  @Column(name = "evidence", columnDefinition = "text")
  private String evidence;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private ReportStatus status;

  @Column(name = "assigned_operator_id")
  private UUID assignedOperatorId;

  @Column(name = "resolution", columnDefinition = "text")
  private String resolution;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private Report(
      UUID reporterUserId,
      ReportTargetType targetType,
      UUID targetId,
      String reason,
      String evidence) {
    this.reporterUserId = reporterUserId;
    this.targetType = targetType;
    this.targetId = targetId;
    this.reason = reason;
    this.evidence = evidence;
    this.status = ReportStatus.SUBMITTED;
  }

  public static Report create(
      UUID reporterUserId,
      ReportTargetType targetType,
      UUID targetId,
      String reason,
      String evidence) {
    Objects.requireNonNull(reporterUserId, "reporterUserId");
    Objects.requireNonNull(targetType, "targetType");
    Objects.requireNonNull(targetId, "targetId");
    Objects.requireNonNull(reason, "reason");
    if (reason.isBlank()) {
      throw new IllegalArgumentException("reason must not be blank");
    }

    return new Report(reporterUserId, targetType, targetId, reason, evidence);
  }

  public void review(UUID operatorUserId) {
    Objects.requireNonNull(operatorUserId, "operatorUserId");
    if (status == ReportStatus.RESOLVED || status == ReportStatus.REJECTED) {
      throw new IllegalStateException("Report is already closed");
    }

    this.assignedOperatorId = operatorUserId;
    this.status = ReportStatus.REVIEWING;
  }

  public void resolve(
      UUID operatorUserId, ReportStatus status, String resolution, Instant resolvedAt) {
    Objects.requireNonNull(operatorUserId, "operatorUserId");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(resolution, "resolution");
    Objects.requireNonNull(resolvedAt, "resolvedAt");
    if (status != ReportStatus.RESOLVED && status != ReportStatus.REJECTED) {
      throw new IllegalArgumentException("status must close report");
    }
    if (resolution.isBlank()) {
      throw new IllegalArgumentException("resolution must not be blank");
    }

    this.assignedOperatorId = operatorUserId;
    this.status = status;
    this.resolution = resolution;
    this.resolvedAt = resolvedAt;
  }
}
