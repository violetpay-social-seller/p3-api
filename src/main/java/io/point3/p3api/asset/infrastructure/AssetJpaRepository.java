package io.point3.p3api.asset.infrastructure;

import io.point3.p3api.asset.domain.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetJpaRepository extends JpaRepository<Asset,UUID> {
}
