package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreWeeklyPickupSettingJpaRepository
    extends JpaRepository<StoreWeeklyPickupSetting, UUID> {

  List<StoreWeeklyPickupSetting> findAllByStoreIdOrderByDayOfWeekAsc(UUID storeId);

  void deleteAllByStoreId(UUID storeId);
}
