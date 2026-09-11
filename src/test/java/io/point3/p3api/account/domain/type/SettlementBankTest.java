package io.point3.p3api.account.domain.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SettlementBankTest {

  @Test
  void resolvesBankByStandardCode() {
    SettlementBank bank = SettlementBank.findByCode("088").orElseThrow();

    assertEquals("신한은행", bank.displayName());
    assertTrue(SettlementBank.findByCode("999").isEmpty());
  }
}
