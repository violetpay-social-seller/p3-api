package io.point3.p3api.assetvariant;

import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import java.util.List;

public record AssetVariantRequest(List<Variant> variants) {

    public record Variant(
            AssetVariantType type,
            String objectKey,
            String contentType,
            int width,
            int height,
            long sizeBytes) {}
}
