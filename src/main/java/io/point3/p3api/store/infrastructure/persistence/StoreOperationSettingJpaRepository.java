package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.domain.entity.StoreOperationSetting;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreOperationSettingJpaRepository
    extends JpaRepository<StoreOperationSetting, UUID> {}
