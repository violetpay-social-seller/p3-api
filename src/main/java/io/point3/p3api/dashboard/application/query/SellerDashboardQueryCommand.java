package io.point3.p3api.dashboard.application.query;

import java.util.Objects;
import java.util.UUID;

public record SellerDashboardQueryCommand(UUID storeId, UUID sellerUserId) {

  public SellerDashboardQueryCommand {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(sellerUserId, "sellerUserId");
  }

  public static SellerDashboardQueryCommand of(UUID storeId, UUID sellerUserId) {
    return new SellerDashboardQueryCommand(storeId, sellerUserId);
  }
}
