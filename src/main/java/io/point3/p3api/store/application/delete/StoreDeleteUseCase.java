package io.point3.p3api.store.application.delete;

import java.util.UUID;

public interface StoreDeleteUseCase {

  void deleteMyStore(UUID ownerUserId);
}
