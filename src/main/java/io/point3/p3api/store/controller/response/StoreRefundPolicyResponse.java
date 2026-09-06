package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.refundpolicy.result.StoreRefundPolicyResult;
import java.util.List;

public record StoreRefundPolicyResponse(List<Rule> rules) {

  public StoreRefundPolicyResponse {
    rules = List.copyOf(rules);
  }

  public static StoreRefundPolicyResponse from(StoreRefundPolicyResult result) {
    return new StoreRefundPolicyResponse(result.rules().stream()
        .map(rule -> new Rule(rule.daysBeforePickup(), rule.refundRate()))
        .toList());
  }

  @Override
  public List<Rule> rules() {
    return List.copyOf(rules);
  }

  public record Rule(int daysBeforePickup, int refundRate) {}
}
