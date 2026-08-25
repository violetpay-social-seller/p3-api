package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.application.result.OrderCalendarDayResult;
import io.point3.p3api.order.application.result.OrderCalendarOrderResult;
import io.point3.p3api.order.application.result.OrderCalendarResult;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record OrderCalendarResponse(
    LocalDate startDate,
    LocalDate endDate,
    OrderStatus status,
    long totalOrderCount,
    List<Day> days) {

  public OrderCalendarResponse {
    days = List.copyOf(days);
  }

  public static OrderCalendarResponse from(OrderCalendarResult result) {
    return new OrderCalendarResponse(
        result.startDate(),
        result.endDate(),
        result.status(),
        result.totalOrderCount(),
        result.days().stream().map(Day::from).toList());
  }

  @Override
  public List<Day> days() {
    return List.copyOf(days);
  }

  public record Day(LocalDate date, long orderCount, List<OrderItem> orders) {

    public Day {
      orders = List.copyOf(orders);
    }

    static Day from(OrderCalendarDayResult result) {
      return new Day(
          result.date(),
          result.orderCount(),
          result.orders().stream().map(OrderItem::from).toList());
    }

    @Override
    public List<OrderItem> orders() {
      return List.copyOf(orders);
    }
  }

  public record OrderItem(
      UUID orderId,
      UUID inquiryId,
      UUID buyerUserId,
      String orderNumber,
      String menuName,
      long paidAmount,
      Instant pickupAt,
      LocalDate pickupDate,
      LocalTime pickupTime,
      OrderStatus status) {

    static OrderItem from(OrderCalendarOrderResult result) {
      return new OrderItem(
          result.orderId(),
          result.inquiryId(),
          result.buyerUserId(),
          result.orderNumber(),
          result.menuName(),
          result.paidAmount(),
          result.pickupAt(),
          result.pickupDate(),
          result.pickupTime(),
          result.status());
    }
  }
}
