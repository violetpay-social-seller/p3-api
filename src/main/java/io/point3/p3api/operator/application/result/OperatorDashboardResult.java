package io.point3.p3api.operator.application.result;

import java.time.LocalDate;
import java.util.List;

public record OperatorDashboardResult(
    long totalUsers,
    long totalSellers,
    long totalStores,
    long pendingOnboardingCount,
    long openReportCount,
    long openServiceInquiryCount,
    LocalDate startDate,
    LocalDate endDate,
    long paymentAmount,
    long refundAmount,
    long netPaymentAmount,
    List<StatusCountResult> orderStatusCounts,
    List<StatusCountResult> paymentStatusCounts,
    List<StatusCountResult> refundStatusCounts) {

  public OperatorDashboardResult {
    orderStatusCounts = List.copyOf(orderStatusCounts);
    paymentStatusCounts = List.copyOf(paymentStatusCounts);
    refundStatusCounts = List.copyOf(refundStatusCounts);
  }

  @Override
  public List<StatusCountResult> orderStatusCounts() {
    return List.copyOf(orderStatusCounts);
  }

  @Override
  public List<StatusCountResult> paymentStatusCounts() {
    return List.copyOf(paymentStatusCounts);
  }

  @Override
  public List<StatusCountResult> refundStatusCounts() {
    return List.copyOf(refundStatusCounts);
  }
}
