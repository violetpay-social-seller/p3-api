package io.point3.p3api.operator.application.query;

import io.point3.p3api.operator.domain.type.ReportStatus;
import io.point3.p3api.operator.domain.type.ReportTargetType;

public record OperatorReportQuery(
    ReportStatus status,
    ReportTargetType targetType,
    String keyword,
    OperatorPageQuery pageQuery) {}
