package io.point3.p3api.store.notice.infrastructure.persistence;

import io.point3.p3api.store.notice.domain.entity.StoreNotice;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreNoticeJpaRepository extends JpaRepository<StoreNotice, UUID> {

  List<StoreNotice> findAllByStoreId(UUID storeId);

  void deleteAllByStoreId(UUID storeId);
}
