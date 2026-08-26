package io.point3.p3api.operator.application.query;

import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.util.UUID;

public record OperatorGalleryItemQuery(
    UUID storeId, UUID assetId, StoreGalleryItemStatus status, OperatorPageQuery pageQuery) {}
