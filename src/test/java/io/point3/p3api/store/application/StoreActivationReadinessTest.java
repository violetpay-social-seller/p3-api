package io.point3.p3api.store.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.exception.code.StoreErrorCode;
import org.junit.jupiter.api.Test;

class StoreActivationReadinessTest {

  @Test
  void canActivateOnlyWhenAllFiveManagementItemsAreComplete() {
    StoreActivationReadiness readiness = new StoreActivationReadiness(true, true, true, true, true);

    assertEquals(5, readiness.completedCount());
    assertTrue(readiness.canActivate());
    assertTrue(readiness.blockedReasons().isEmpty());
    assertNull(readiness.firstBlockingError());
  }

  @Test
  void blocksActivationWhenStoreInformationIsIncomplete() {
    StoreActivationReadiness readiness =
        new StoreActivationReadiness(false, true, true, true, true);

    assertEquals(4, readiness.completedCount());
    assertFalse(readiness.canActivate());
    assertTrue(readiness.blockedReasons().contains("STORE_INFORMATION_REQUIRED"));
    assertEquals(StoreErrorCode.STORE_INFORMATION_REQUIRED, readiness.firstBlockingError());
  }

  @Test
  void preservesRepresentativeImageErrorPriority() {
    StoreActivationReadiness readiness =
        new StoreActivationReadiness(false, false, false, false, false);

    assertEquals(
        StoreErrorCode.REPRESENTATIVE_IMAGE_MINIMUM_REQUIRED, readiness.firstBlockingError());
  }
}
