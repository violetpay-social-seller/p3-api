package io.point3.p3api.store.application.refundpolicy.result;

import java.util.List;

public record StoreRefundPolicyResult(List<Rule> rules) {

  public StoreRefundPolicyResult {
    rules = List.copyOf(rules);
  }

  @Override
  public List<Rule> rules() {
    return List.copyOf(rules);
  }

  public record Rule(int daysBeforePickup, int refundRate) {}
}
