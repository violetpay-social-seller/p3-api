package io.point3.p3api.asset.application.port;

import io.point3.p3api.asset.domain.entity.Asset;

public interface AssetPersistencePort {

    Asset save(Asset asset);

}
