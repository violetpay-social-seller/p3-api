package io.point3.p3api.assetvariant.infrastructure;

import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetVariantJpaRepository extends JpaRepository<AssetVariant, UUID> {}
