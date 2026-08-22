package io.point3.p3api.store.application.setting.port;

import io.point3.p3api.store.domain.entity.StoreHoliday;
import java.util.List;
import java.util.UUID;

public interface StoreHolidayPersistencePort {

  List<StoreHoliday> saveAll(List<StoreHoliday> holidays);

  List<StoreHoliday> findAllByStoreId(UUID storeId);

  void deleteAllByStoreId(UUID storeId);
}
