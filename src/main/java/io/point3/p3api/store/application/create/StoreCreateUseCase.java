package io.point3.p3api.store.application.create;

import io.point3.p3api.store.application.result.StoreResult;

public interface StoreCreateUseCase {

  StoreResult create(CreateStoreCommand command);
}
