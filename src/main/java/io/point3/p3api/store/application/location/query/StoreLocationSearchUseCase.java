package io.point3.p3api.store.application.location.query;

import io.point3.p3api.store.application.location.command.SearchStoreLocationCommand;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.util.List;

public interface StoreLocationSearchUseCase {

  List<StoreLocationResult> search(SearchStoreLocationCommand command);
}
