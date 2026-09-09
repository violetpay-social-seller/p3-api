package io.point3.p3api.store.application.management;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.StoreActivationReadiness;
import io.point3.p3api.store.application.StoreActivationReadinessChecker;
import io.point3.p3api.store.application.management.result.StoreManagementStatusResult;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreManagementStatusQueryService implements StoreManagementStatusQueryUseCase {

  private final StorePersistencePort storePersistencePort;
  private final StoreActivationReadinessChecker readinessChecker;

  @Override
  public StoreManagementStatusResult getStatus(UUID storeId) {
    Store store = storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
    StoreActivationReadiness readiness = readinessChecker.check(store);
    return new StoreManagementStatusResult(
        store.getName(),
        readiness.completedCount(),
        5,
        new StoreManagementStatusResult.Items(
            readiness.storeInfo(),
            readiness.orderForm(),
            readiness.notice(),
            readiness.photoRegistration(),
            readiness.settlementAccount()),
        readiness.canActivate(),
        readiness.blockedReasons());
  }
}
