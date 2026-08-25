package io.point3.p3api.dashboard.application.result;

import java.time.LocalDate;

public record SellerRevenueResult(
    LocalDate startDate,
    LocalDate endDate,
    long paymentRevenueAmount,
    long completedRefundAmount,
    long netSalesAmount,
    int settlementFeeRateBasisPoints,
    long settlementFeeAmount,
    long settlementEstimateAmount) {

  public static SellerRevenueResult of(
      LocalDate startDate,
      LocalDate endDate,
      long paymentRevenueAmount,
      long completedRefundAmount,
      int settlementFeeRateBasisPoints) {
    long netSalesAmount = paymentRevenueAmount - completedRefundAmount;
    long settlementFeeAmount = calculateFee(netSalesAmount, settlementFeeRateBasisPoints);

    return new SellerRevenueResult(
        startDate,
        endDate,
        paymentRevenueAmount,
        completedRefundAmount,
        netSalesAmount,
        settlementFeeRateBasisPoints,
        settlementFeeAmount,
        netSalesAmount - settlementFeeAmount);
  }

  private static long calculateFee(long netSalesAmount, int feeRateBasisPoints) {
    if (netSalesAmount <= 0) {
      return 0;
    }

    return netSalesAmount * feeRateBasisPoints / 10_000;
  }
}
