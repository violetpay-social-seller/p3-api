package io.point3.p3api.dashboard.controller.response;

import io.point3.p3api.dashboard.application.result.SellerDashboardResult;
import io.point3.p3api.order.application.result.OrderCalendarOrderResult;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record SellerDashboardResponse(
    LocalDate today,
    LocalDate weekStartDate,
    LocalDate weekEndDate,
    SellerRevenueResponse currentMonthRevenue,
    long todayOrderCount,
    long thisWeekOrderCount,
    long paidOrderCount,
    long cancelRefundRequestCount,
    long unansweredInquiryCount,
    List<TodayOrder> todayOrders) {

  public SellerDashboardResponse {
    todayOrders = List.copyOf(todayOrders);
  }

  public static SellerDashboardResponse from(SellerDashboardResult result) {
    return new SellerDashboardResponse(
        result.today(),
        result.weekStartDate(),
        result.weekEndDate(),
        SellerRevenueResponse.from(result.currentMonthRevenue()),
        result.todayOrderCount(),
        result.thisWeekOrderCount(),
        result.paidOrderCount(),
        result.cancelRefundRequestCount(),
        result.unansweredInquiryCount(),
        result.todayOrders().stream().map(TodayOrder::from).toList());
  }

  @Override
  public List<TodayOrder> todayOrders() {
    return List.copyOf(todayOrders);
  }

  public record TodayOrder(
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

    public static TodayOrder from(OrderCalendarOrderResult result) {
      return new TodayOrder(
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
