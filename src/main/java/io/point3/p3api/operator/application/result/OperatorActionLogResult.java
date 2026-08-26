package io.point3.p3api.operator.application.result;

import io.point3.p3api.operator.domain.entity.OperatorActionLog;
import io.point3.p3api.operator.domain.type.OperatorActionType;
import io.point3.p3api.operator.domain.type.OperatorTargetType;
import java.time.Instant;
import java.util.UUID;

public record OperatorActionLogResult(
    UUID id,
    UUID operatorUserId,
    OperatorActionType actionType,
    OperatorTargetType targetType,
    UUID targetId,
    String reason,
    Instant createdAt) {

  public static OperatorActionLogResult from(OperatorActionLog actionLog) {
    return new OperatorActionLogResult(
        actionLog.getId(),
        actionLog.getOperatorUserId(),
        actionLog.getActionType(),
        actionLog.getTargetType(),
        actionLog.getTargetId(),
        actionLog.getReason(),
        actionLog.getCreatedAt());
  }
}
