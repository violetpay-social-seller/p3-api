package io.point3.p3api.order.application.refund;

import io.point3.p3api.store.domain.entity.StoreRefundPolicy;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class OrderRefundPolicyCalculator {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

  public RefundCalculation calculate(
      long paidAmount, Instant pickupAt, Instant requestedAt, List<StoreRefundPolicy> policies) {
    if (paidAmount < 0) {
      throw new IllegalArgumentException("paidAmount must be greater than or equal to 0");
    }
    Objects.requireNonNull(pickupAt, "pickupAt");
    Objects.requireNonNull(requestedAt, "requestedAt");
    Objects.requireNonNull(policies, "policies");

    long daysBeforePickup = ChronoUnit.DAYS.between(
        requestedAt.atZone(KOREA_ZONE_ID).toLocalDate(),
        pickupAt.atZone(KOREA_ZONE_ID).toLocalDate());
    int refundRate = policies.stream()
        .peek(this::validatePolicy)
        .filter(policy -> daysBeforePickup >= policy.getDaysBeforePickup())
        .max(Comparator.comparingInt(StoreRefundPolicy::getDaysBeforePickup))
        .map(StoreRefundPolicy::getRefundRate)
        .orElse(0);

    return new RefundCalculation(refundRate, calculateAmount(paidAmount, refundRate));
  }

  private long calculateAmount(long paidAmount, int refundRate) {
    long amountFromWholeHundreds = (paidAmount / 100) * refundRate;
    long amountFromRemainder = ((paidAmount % 100) * refundRate) / 100;
    return Math.addExact(amountFromWholeHundreds, amountFromRemainder);
  }

  private void validatePolicy(StoreRefundPolicy policy) {
    Objects.requireNonNull(policy, "policy");
    if (policy.getDaysBeforePickup() < 0
        || policy.getRefundRate() < 0
        || policy.getRefundRate() > 100) {
      throw new IllegalArgumentException("refund policy is invalid");
    }
  }

  public record RefundCalculation(int refundRate, long amount) {}
}
