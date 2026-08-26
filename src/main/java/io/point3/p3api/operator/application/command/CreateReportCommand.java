package io.point3.p3api.operator.application.command;

import io.point3.p3api.operator.domain.type.ReportTargetType;
import java.util.UUID;

public record CreateReportCommand(
    UUID reporterUserId,
    ReportTargetType targetType,
    UUID targetId,
    String reason,
    String evidence) {

  public static CreateReportCommand of(
      UUID reporterUserId,
      ReportTargetType targetType,
      UUID targetId,
      String reason,
      String evidence) {
    return new CreateReportCommand(reporterUserId, targetType, targetId, reason, evidence);
  }
}
