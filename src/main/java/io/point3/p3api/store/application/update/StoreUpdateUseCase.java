package io.point3.p3api.store.application.update;

import io.point3.p3api.store.application.result.StoreResult;

public interface StoreUpdateUseCase {

  StoreResult update(UpdateStoreCommand command);

  StoreResult changeStatus(ChangeStoreStatusCommand command);
}
