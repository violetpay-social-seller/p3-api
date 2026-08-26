package io.point3.p3api.operator.application.result;

import io.point3.p3api.operator.domain.entity.Report;
import io.point3.p3api.operator.domain.type.ReportStatus;
import io.point3.p3api.operator.domain.type.ReportTargetType;
import java.time.Instant;
import java.util.UUID;

public record OperatorReportResult(
    UUID id,
    UUID reporterUserId,
    ReportTargetType targetType,
    UUID targetId,
    String reason,
    String evidence,
    ReportStatus status,
    UUID assignedOperatorId,
    String resolution,
    Instant resolvedAt,
    Instant createdAt,
    Instant updatedAt) {

  public static OperatorReportResult from(Report report) {
    return new OperatorReportResult(
        report.getId(),
        report.getReporterUserId(),
        report.getTargetType(),
        report.getTargetId(),
        report.getReason(),
        report.getEvidence(),
        report.getStatus(),
        report.getAssignedOperatorId(),
        report.getResolution(),
        report.getResolvedAt(),
        report.getCreatedAt(),
        report.getUpdatedAt());
  }
}
