package io.point3.p3api.store.application.refundpolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.store.application.refundpolicy.command.UpdateStoreRefundPolicyCommand;
import java.util.List;
import org.junit.jupiter.api.Test;

class StoreRefundPolicyTextFormatterTest {

  private final StoreRefundPolicyTextFormatter formatter = new StoreRefundPolicyTextFormatter();

  @Test
  void formatsRulesForBuyerPolicyText() {
    String text = formatter.format(List.of(
        new UpdateStoreRefundPolicyCommand.Rule(7, 100),
        new UpdateStoreRefundPolicyCommand.Rule(5, 80)));

    assertEquals("픽업일 7일 전까지 100% 환불, 픽업일 5일 전까지 80% 환불", text);
  }
}
