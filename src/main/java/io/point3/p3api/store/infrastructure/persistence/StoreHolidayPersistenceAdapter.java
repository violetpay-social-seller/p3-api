package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.application.setting.port.StoreHolidayPersistencePort;
import io.point3.p3api.store.domain.entity.StoreHoliday;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class StoreHolidayPersistenceAdapter implements StoreHolidayPersistencePort {

  private final StoreHolidayJpaRepository storeHolidayJpaRepository;

  @Override
  public List<StoreHoliday> saveAll(List<StoreHoliday> holidays) {
    return storeHolidayJpaRepository.saveAll(holidays);
  }

  @Override
  public List<StoreHoliday> findAllByStoreId(UUID storeId) {
    return storeHolidayJpaRepository.findAllByStoreIdOrderByHolidayDateAsc(storeId);
  }

  @Override
  public void deleteAllByStoreId(UUID storeId) {
    storeHolidayJpaRepository.deleteAllByStoreId(storeId);
  }
}
