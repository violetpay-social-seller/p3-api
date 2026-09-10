package io.point3.p3api.store.application.profileimage;

import java.util.Objects;
import java.util.UUID;

public record UpdateSellerProfileImageCommand(UUID storeId, UUID userId, UUID profileAssetId) {

  public UpdateSellerProfileImageCommand {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(userId, "userId");
  }
}
