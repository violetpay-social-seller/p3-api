package io.point3.p3api.operator.application.query;

import io.point3.p3api.operator.domain.type.OperatorActionType;
import io.point3.p3api.operator.domain.type.OperatorTargetType;
import java.time.LocalDate;
import java.util.UUID;

public record OperatorActionLogQuery(
    UUID operatorUserId,
    OperatorActionType actionType,
    OperatorTargetType targetType,
    UUID targetId,
    LocalDate startDate,
    LocalDate endDate,
    OperatorPageQuery pageQuery) {}
