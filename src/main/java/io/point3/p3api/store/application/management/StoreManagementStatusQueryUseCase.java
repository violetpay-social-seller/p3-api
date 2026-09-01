package io.point3.p3api.store.application.management;

import io.point3.p3api.store.application.management.result.StoreManagementStatusResult;
import java.util.UUID;

public interface StoreManagementStatusQueryUseCase {

  StoreManagementStatusResult getStatus(UUID storeId);
}
