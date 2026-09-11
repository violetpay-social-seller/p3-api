package io.point3.p3api.store.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.point3.p3api.account.application.settlement.port.SellerSettlementAccountPersistencePort;
import io.point3.p3api.orderform.application.port.OrderFormPersistencePort;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.representative.port.RepresentativeImagePersistencePort;
import io.point3.p3api.store.application.setting.port.StoreWeeklyPickupSettingPersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoreActivationReadinessCheckerTest {

  private final OrderFormPersistencePort orderFormPersistencePort =
      mock(OrderFormPersistencePort.class);
  private final StoreNoticePersistencePort storeNoticePersistencePort =
      mock(StoreNoticePersistencePort.class);
  private final StoreWeeklyPickupSettingPersistencePort weeklyPickupSettingPersistencePort =
      mock(StoreWeeklyPickupSettingPersistencePort.class);
  private final RepresentativeImagePersistencePort representativeImagePersistencePort =
      mock(RepresentativeImagePersistencePort.class);
  private final SellerSettlementAccountPersistencePort settlementAccountPersistencePort =
      mock(SellerSettlementAccountPersistencePort.class);
  private final StoreActivationReadinessChecker checker = new StoreActivationReadinessChecker(
      orderFormPersistencePort,
      storeNoticePersistencePort,
      weeklyPickupSettingPersistencePort,
      representativeImagePersistencePort,
      settlementAccountPersistencePort);

  @Test
  void usesVerifiedSettlementAccountExistenceAsCompletionSource() {
    UUID storeId = UUID.randomUUID();
    Store store = mock(Store.class);
    when(store.getId()).thenReturn(storeId);
    when(weeklyPickupSettingPersistencePort.findAllByStoreId(storeId)).thenReturn(List.of());
    when(representativeImagePersistencePort.findActiveByStoreId(storeId)).thenReturn(List.of());

    when(settlementAccountPersistencePort.existsByStoreId(storeId)).thenReturn(false);
    assertFalse(checker.check(store).settlementAccount());

    when(settlementAccountPersistencePort.existsByStoreId(storeId)).thenReturn(true);
    assertTrue(checker.check(store).settlementAccount());
  }
}
