package io.point3.p3api.payment.application.port;

import java.util.List;
import java.util.Optional;

public record Point3RefundStatusResult(
    String paymentSessionId,
    String status,
    long refundableAmount,
    boolean canCreateRefund,
    List<Point3RefundResult> refunds) {

  public Point3RefundStatusResult {
    refunds = refunds == null ? List.of() : List.copyOf(refunds);
  }

  public Optional<Point3RefundResult> findRefund(String providerRefundId) {
    if (providerRefundId != null && !providerRefundId.isBlank()) {
      Optional<Point3RefundResult> matched = refunds.stream()
          .filter(refund -> providerRefundId.equals(refund.providerRefundId()))
          .findFirst();
      if (matched.isPresent()) {
        return matched;
      }
      return Optional.empty();
    }
    if (refunds.size() == 1) {
      return Optional.of(refunds.get(0));
    }
    return Optional.empty();
  }

  public boolean hasAmbiguousRefundsWithoutProviderId(String providerRefundId) {
    return (providerRefundId == null || providerRefundId.isBlank()) && refunds.size() > 1;
  }
}
