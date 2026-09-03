package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.domain.entity.StoreNotice;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class StoreNoticePersistenceAdapter implements StoreNoticePersistencePort {

  private final StoreNoticeJpaRepository storeNoticeJpaRepository;

  @Override
  @Transactional(readOnly = true)
  public List<StoreNotice> findAllByStoreId(UUID storeId) {
    return storeNoticeJpaRepository.findAllByStoreId(storeId);
  }

  @Override
  public List<StoreNotice> replaceAllByStoreId(UUID storeId, List<StoreNotice> notices) {
    storeNoticeJpaRepository.deleteAllByStoreId(storeId);
    storeNoticeJpaRepository.flush();
    return storeNoticeJpaRepository.saveAll(notices);
  }
}
