package io.point3.p3api.store.application.refundpolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.refundpolicy.command.UpdateStoreRefundPolicyCommand;
import io.point3.p3api.store.application.refundpolicy.port.StoreRefundPolicyPersistencePort;
import io.point3.p3api.store.application.refundpolicy.result.StoreRefundPolicyResult;
import io.point3.p3api.store.domain.entity.Store;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoreRefundPolicyServiceTest {

  private final StorePersistencePort storePersistencePort = mock(StorePersistencePort.class);
  private final StoreRefundPolicyPersistencePort refundPolicyPersistencePort =
      mock(StoreRefundPolicyPersistencePort.class);
  private final StoreRefundPolicyService service = new StoreRefundPolicyService(
      storePersistencePort, refundPolicyPersistencePort, new StoreRefundPolicyTextFormatter());

  @Test
  void replacesPoliciesInRequestOrder() {
    UUID storeId = UUID.randomUUID();
    UpdateStoreRefundPolicyCommand command = new UpdateStoreRefundPolicyCommand(
        storeId,
        List.of(
            new UpdateStoreRefundPolicyCommand.Rule(7, 100),
            new UpdateStoreRefundPolicyCommand.Rule(5, 80)));
    Store store = Store.create(UUID.randomUUID(), "스토어", "store");
    when(storePersistencePort.findById(storeId)).thenReturn(Optional.of(store));
    when(refundPolicyPersistencePort.replaceAllByStoreId(eq(storeId), any()))
        .thenAnswer(invocation -> invocation.getArgument(1));

    StoreRefundPolicyResult result = service.updateRefundPolicy(command);

    assertEquals(
        List.of(new StoreRefundPolicyResult.Rule(7, 100), new StoreRefundPolicyResult.Rule(5, 80)),
        result.rules());
    assertEquals("픽업일 7일 전까지 100% 환불\n픽업일 5일 전까지 80% 환불", store.getCancellationRefundPolicy());
    verify(storePersistencePort).save(store);
  }

  @Test
  void rejectsRatesOutsideTenPercentIncrementsAndInvalidOrder() {
    UUID storeId = UUID.randomUUID();

    assertThrows(
        BaseException.class,
        () -> service.updateRefundPolicy(new UpdateStoreRefundPolicyCommand(
            storeId, List.of(new UpdateStoreRefundPolicyCommand.Rule(7, 85)))));
    assertThrows(
        BaseException.class,
        () -> service.updateRefundPolicy(new UpdateStoreRefundPolicyCommand(
            storeId,
            List.of(
                new UpdateStoreRefundPolicyCommand.Rule(5, 100),
                new UpdateStoreRefundPolicyCommand.Rule(7, 80)))));
  }

  @Test
  void returnsEmptyRulesWhenPolicyIsNotConfigured() {
    UUID storeId = UUID.randomUUID();
    when(storePersistencePort.findById(storeId))
        .thenReturn(Optional.of(Store.create(UUID.randomUUID(), "스토어", "store")));
    when(refundPolicyPersistencePort.findAllByStoreId(storeId)).thenReturn(List.of());

    StoreRefundPolicyResult result = service.getRefundPolicy(storeId);

    assertEquals(List.of(), result.rules());
  }
}
