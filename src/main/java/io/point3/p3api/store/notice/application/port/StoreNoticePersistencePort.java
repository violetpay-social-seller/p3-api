package io.point3.p3api.store.notice.application.port;

import io.point3.p3api.store.notice.domain.entity.StoreNotice;
import java.util.List;
import java.util.UUID;

public interface StoreNoticePersistencePort {

  List<StoreNotice> findAllByStoreId(UUID storeId);

  List<StoreNotice> replaceAllByStoreId(UUID storeId, List<StoreNotice> notices);
}
