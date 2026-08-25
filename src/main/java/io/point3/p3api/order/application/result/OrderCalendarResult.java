package io.point3.p3api.order.application.result;

import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.LocalDate;
import java.util.List;

public record OrderCalendarResult(
    LocalDate startDate,
    LocalDate endDate,
    OrderStatus status,
    long totalOrderCount,
    List<OrderCalendarDayResult> days) {

  public OrderCalendarResult {
    days = List.copyOf(days);
  }

  public static OrderCalendarResult of(
      LocalDate startDate,
      LocalDate endDate,
      OrderStatus status,
      List<OrderCalendarDayResult> days) {
    long totalOrderCount =
        days.stream().mapToLong(OrderCalendarDayResult::orderCount).sum();

    return new OrderCalendarResult(startDate, endDate, status, totalOrderCount, days);
  }

  @Override
  public List<OrderCalendarDayResult> days() {
    return List.copyOf(days);
  }
}
