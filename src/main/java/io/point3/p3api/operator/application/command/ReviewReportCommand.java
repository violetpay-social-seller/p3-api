package io.point3.p3api.operator.application.command;

import java.util.UUID;

public record ReviewReportCommand(UUID reportId, UUID operatorUserId) {

  public static ReviewReportCommand of(UUID reportId, UUID operatorUserId) {
    return new ReviewReportCommand(reportId, operatorUserId);
  }
}
