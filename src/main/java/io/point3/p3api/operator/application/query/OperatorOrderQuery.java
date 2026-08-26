package io.point3.p3api.operator.application.query;

import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.LocalDate;
import java.util.UUID;

public record OperatorOrderQuery(
    UUID storeId,
    UUID buyerUserId,
    OrderStatus status,
    LocalDate startDate,
    LocalDate endDate,
    OperatorPageQuery pageQuery) {}
