package io.point3.p3api.store.application.refundpolicy;

import io.point3.p3api.store.application.refundpolicy.command.UpdateStoreRefundPolicyCommand;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StoreRefundPolicyTextFormatter {

  public String format(List<UpdateStoreRefundPolicyCommand.Rule> rules) {
    return rules.stream()
        .map(rule -> "픽업일 " + rule.daysBeforePickup() + "일 전까지 " + rule.refundRate() + "% 환불")
        .collect(java.util.stream.Collectors.joining("\n"));
  }
}
