package io.point3.p3api.store.application.refundpolicy.command;

import java.util.List;
import java.util.UUID;

public record UpdateStoreRefundPolicyCommand(UUID storeId, List<Rule> rules) {

  public UpdateStoreRefundPolicyCommand {
    rules = rules == null ? List.of() : List.copyOf(rules);
  }

  public record Rule(int daysBeforePickup, int refundRate) {}
}
