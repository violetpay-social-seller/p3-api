package io.point3.p3api.asset.infrastructure.persistence;

import io.point3.p3api.asset.domain.entity.Asset;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetJpaRepository extends JpaRepository<Asset, UUID> {
  Optional<Asset> findByIdAndUploadedBy(UUID assetId, UUID uploadedBy);

  List<Asset> findAllByUploadedByOrderByCreatedAtDesc(UUID uploadedBy);
}
