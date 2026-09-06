package io.point3.p3api.store.application.refundpolicy;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.refundpolicy.command.UpdateStoreRefundPolicyCommand;
import io.point3.p3api.store.application.refundpolicy.port.StoreRefundPolicyPersistencePort;
import io.point3.p3api.store.application.refundpolicy.query.StoreRefundPolicyQueryUseCase;
import io.point3.p3api.store.application.refundpolicy.result.StoreRefundPolicyResult;
import io.point3.p3api.store.application.refundpolicy.update.StoreRefundPolicyUpdateUseCase;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.entity.StoreRefundPolicy;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreRefundPolicyService
    implements StoreRefundPolicyQueryUseCase, StoreRefundPolicyUpdateUseCase {

  private final StorePersistencePort storePersistencePort;
  private final StoreRefundPolicyPersistencePort storeRefundPolicyPersistencePort;
  private final StoreRefundPolicyTextFormatter refundPolicyTextFormatter;

  @Override
  @Transactional(readOnly = true)
  public StoreRefundPolicyResult getRefundPolicy(UUID storeId) {
    requireStore(storeId);
    return new StoreRefundPolicyResult(
        storeRefundPolicyPersistencePort.findAllByStoreId(storeId).stream()
            .map(policy -> new StoreRefundPolicyResult.Rule(
                policy.getDaysBeforePickup(), policy.getRefundRate()))
            .toList());
  }

  @Override
  public StoreRefundPolicyResult updateRefundPolicy(UpdateStoreRefundPolicyCommand command) {
    validate(command);
    Store store = requireStore(command.storeId());
    List<StoreRefundPolicy> policies = java.util.stream.IntStream.range(
            0, command.rules().size())
        .mapToObj(index -> {
          UpdateStoreRefundPolicyCommand.Rule rule = command.rules().get(index);
          return StoreRefundPolicy.create(
              command.storeId(), rule.daysBeforePickup(), rule.refundRate(), index);
        })
        .toList();
    List<StoreRefundPolicy> saved =
        storeRefundPolicyPersistencePort.replaceAllByStoreId(command.storeId(), policies);
    store.updateCancellationRefundPolicy(refundPolicyTextFormatter.format(command.rules()));
    storePersistencePort.save(store);
    return new StoreRefundPolicyResult(saved.stream()
        .map(policy ->
            new StoreRefundPolicyResult.Rule(policy.getDaysBeforePickup(), policy.getRefundRate()))
        .toList());
  }

  private void validate(UpdateStoreRefundPolicyCommand command) {
    if (command == null || command.storeId() == null || command.rules().isEmpty()) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
    int previousDaysBeforePickup = Integer.MAX_VALUE;
    int previousRefundRate = Integer.MAX_VALUE;
    for (UpdateStoreRefundPolicyCommand.Rule rule : command.rules()) {
      if (rule == null
          || rule.daysBeforePickup() < 0
          || rule.refundRate() < 0
          || rule.refundRate() > 100
          || rule.refundRate() % 10 != 0
          || rule.daysBeforePickup() >= previousDaysBeforePickup
          || rule.refundRate() >= previousRefundRate) {
        throw new BaseException(CommonErrorCode.INVALID_INPUT);
      }
      previousDaysBeforePickup = rule.daysBeforePickup();
      previousRefundRate = rule.refundRate();
    }
  }

  private Store requireStore(UUID storeId) {
    return storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
  }
}
