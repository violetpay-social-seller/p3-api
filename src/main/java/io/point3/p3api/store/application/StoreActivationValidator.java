package io.point3.p3api.store.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.domain.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreActivationValidator {

  private final StoreActivationReadinessChecker readinessChecker;

  public void validate(Store store) {
    StoreErrorCode blockingError = readinessChecker.check(store).firstBlockingError();
    if (blockingError != null) {
      throw new BaseException(blockingError);
    }
  }
}
