package io.point3.p3api.inquiry.application.submit;

import java.util.UUID;

public record SubmitPreOrderCommand(UUID storeId, UUID buyerUserId, UUID productId) {

  public static SubmitPreOrderCommand of(UUID storeId, UUID buyerUserId, UUID productId) {
    return new SubmitPreOrderCommand(storeId, buyerUserId, productId);
  }
}
