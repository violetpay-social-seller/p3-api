package io.point3.p3api.dashboard.application.result;

import io.point3.p3api.order.application.result.OrderCalendarOrderResult;
import java.time.LocalDate;
import java.util.List;

public record SellerDashboardResult(
    LocalDate today,
    LocalDate weekStartDate,
    LocalDate weekEndDate,
    SellerRevenueResult currentMonthRevenue,
    long todayOrderCount,
    long thisWeekOrderCount,
    long paidOrderCount,
    long cancelRefundRequestCount,
    long unansweredInquiryCount,
    List<OrderCalendarOrderResult> todayOrders) {

  public SellerDashboardResult {
    todayOrders = List.copyOf(todayOrders);
  }

  @Override
  public List<OrderCalendarOrderResult> todayOrders() {
    return List.copyOf(todayOrders);
  }
}
