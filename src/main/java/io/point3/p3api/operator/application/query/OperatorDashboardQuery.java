package io.point3.p3api.operator.application.query;

import java.time.LocalDate;

public record OperatorDashboardQuery(LocalDate startDate, LocalDate endDate) {}
