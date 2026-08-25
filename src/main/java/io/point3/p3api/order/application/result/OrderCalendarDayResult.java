package io.point3.p3api.order.application.result;

import java.time.LocalDate;
import java.util.List;

public record OrderCalendarDayResult(
    LocalDate date, long orderCount, List<OrderCalendarOrderResult> orders) {

  public OrderCalendarDayResult {
    orders = List.copyOf(orders);
  }

  public static OrderCalendarDayResult of(LocalDate date, List<OrderCalendarOrderResult> orders) {
    return new OrderCalendarDayResult(date, orders.size(), orders);
  }

  @Override
  public List<OrderCalendarOrderResult> orders() {
    return List.copyOf(orders);
  }
}
