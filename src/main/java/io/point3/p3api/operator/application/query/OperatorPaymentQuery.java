package io.point3.p3api.operator.application.query;

import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import java.time.LocalDate;

public record OperatorPaymentQuery(
    PaymentAttemptStatus status,
    LocalDate startDate,
    LocalDate endDate,
    OperatorPageQuery pageQuery) {}
