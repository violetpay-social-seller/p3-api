package io.point3.p3api.asset.controller;

import io.point3.p3api.asset.application.result.AssetDetailResult;
import io.point3.p3api.asset.domain.type.AssetStatus;
import java.time.Instant;
import java.util.UUID;

public record AssetDetailResponse(
    UUID id,
    UUID uploadedBy,
    String originalFilename,
    String contentType,
    long size,
    String deliveryUrl,
    AssetStatus status,
    Instant createdAt,
    Instant updatedAt) {
  public static AssetDetailResponse from(AssetDetailResult result) {
    return new AssetDetailResponse(
        result.id(),
        result.uploadedBy(),
        result.originalFilename(),
        result.contentType(),
        result.size(),
        result.deliveryUrl(),
        result.status(),
        result.createdAt(),
        result.updatedAt());
  }
}
