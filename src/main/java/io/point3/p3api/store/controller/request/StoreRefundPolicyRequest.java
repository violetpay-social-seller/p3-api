package io.point3.p3api.store.controller.request;

import io.point3.p3api.store.application.refundpolicy.command.UpdateStoreRefundPolicyCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record StoreRefundPolicyRequest(@NotEmpty List<@NotNull @Valid Rule> rules) {

  public StoreRefundPolicyRequest {
    rules = rules == null ? null : List.copyOf(rules);
  }

  public UpdateStoreRefundPolicyCommand toCommand(UUID storeId) {
    return new UpdateStoreRefundPolicyCommand(
        storeId,
        rules == null
            ? List.of()
            : rules.stream()
                .map(rule -> new UpdateStoreRefundPolicyCommand.Rule(
                    rule.daysBeforePickup(), rule.refundRate()))
                .toList());
  }

  public record Rule(
      @NotNull Integer daysBeforePickup, @NotNull Integer refundRate) {}
}
