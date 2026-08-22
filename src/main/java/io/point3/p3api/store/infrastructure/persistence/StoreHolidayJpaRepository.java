package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.domain.entity.StoreHoliday;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreHolidayJpaRepository extends JpaRepository<StoreHoliday, UUID> {

  List<StoreHoliday> findAllByStoreIdOrderByHolidayDateAsc(UUID storeId);

  void deleteAllByStoreId(UUID storeId);
}
