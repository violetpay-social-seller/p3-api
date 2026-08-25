package io.point3.p3api.order.application.calendar;

import io.point3.p3api.order.application.result.OrderCalendarResult;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.util.UUID;

public interface OrderCalendarQueryUseCase {

  OrderCalendarResult getToday(UUID storeId, OrderStatus status);

  OrderCalendarResult getThisWeek(UUID storeId, OrderStatus status);

  OrderCalendarResult getMonth(UUID storeId, int year, int month, OrderStatus status);
}
