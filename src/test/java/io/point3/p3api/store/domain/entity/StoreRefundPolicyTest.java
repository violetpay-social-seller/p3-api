package io.point3.p3api.store.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoreRefundPolicyTest {

  @Test
  void createsPolicy() {
    UUID storeId = UUID.randomUUID();

    StoreRefundPolicy policy = StoreRefundPolicy.create(storeId, 7, 90, 0);

    assertEquals(storeId, policy.getStoreId());
    assertEquals(7, policy.getDaysBeforePickup());
    assertEquals(90, policy.getRefundRate());
    assertEquals(0, policy.getSortOrder());
  }
}
