package io.point3.p3api.store.controller.request;

import io.point3.p3api.store.application.update.UpdateStoreDescriptionCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record StoreDescriptionRequest(@NotBlank String description) {

  public UpdateStoreDescriptionCommand toCommand(UUID storeId) {
    return new UpdateStoreDescriptionCommand(storeId, description);
  }
}
