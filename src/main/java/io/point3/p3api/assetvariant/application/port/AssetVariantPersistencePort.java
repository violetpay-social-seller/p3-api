package io.point3.p3api.assetvariant.application.port;

import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import java.util.List;

public interface AssetVariantPersistencePort {

    List<AssetVariant> saveAll(List<AssetVariant> variants);
}
