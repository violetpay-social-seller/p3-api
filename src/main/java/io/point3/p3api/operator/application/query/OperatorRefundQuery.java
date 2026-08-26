package io.point3.p3api.operator.application.query;

import io.point3.p3api.payment.domain.type.RefundStatus;
import java.time.LocalDate;

public record OperatorRefundQuery(
    RefundStatus status, LocalDate startDate, LocalDate endDate, OperatorPageQuery pageQuery) {}
