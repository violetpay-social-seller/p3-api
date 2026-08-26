package io.point3.p3api.operator.application.command;

import io.point3.p3api.operator.domain.type.ReportStatus;
import io.point3.p3api.operator.domain.type.ReportTargetAction;
import java.util.UUID;

public record ResolveReportCommand(
    UUID reportId,
    UUID operatorUserId,
    ReportStatus status,
    String resolution,
    ReportTargetAction targetAction) {

  public static ResolveReportCommand of(
      UUID reportId,
      UUID operatorUserId,
      ReportStatus status,
      String resolution,
      ReportTargetAction targetAction) {
    return new ResolveReportCommand(reportId, operatorUserId, status, resolution, targetAction);
  }
}
