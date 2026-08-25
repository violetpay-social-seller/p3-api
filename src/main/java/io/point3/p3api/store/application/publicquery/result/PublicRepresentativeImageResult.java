package io.point3.p3api.store.application.publicquery.result;

import java.util.UUID;

public record PublicRepresentativeImageResult(
    UUID id, UUID assetId, String deliveryUrl, int sortOrder) {}
