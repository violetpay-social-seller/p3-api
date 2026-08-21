package io.point3.p3api.asset.application.result;

import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.domain.type.AssetStatus;
import java.time.Instant;
import java.util.UUID;

public record AssetDetailResult(
    UUID id,
    UUID uploadedBy,
    String originalFilename,
    String contentType,
    long size,
    String deliveryUrl,
    AssetStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static AssetDetailResult from(Asset asset, String deliveryUrl) {
    return new AssetDetailResult(
        asset.getId(),
        asset.getUploadedBy(),
        asset.getOriginalFilename(),
        asset.getContentType(),
        asset.getSize(),
        deliveryUrl,
        asset.getStatus(),
        asset.getCreatedAt(),
        asset.getUpdatedAt());
  }
}
