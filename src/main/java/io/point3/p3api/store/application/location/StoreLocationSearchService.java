package io.point3.p3api.store.application.location;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.location.command.SearchStoreLocationCommand;
import io.point3.p3api.store.application.location.port.StoreLocationSearchException;
import io.point3.p3api.store.application.location.port.StoreLocationSearchPort;
import io.point3.p3api.store.application.location.query.StoreLocationSearchUseCase;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreLocationSearchService implements StoreLocationSearchUseCase {

  private final StoreLocationSearchPort storeLocationSearchPort;

  @Override
  public List<StoreLocationResult> search(SearchStoreLocationCommand command) {
    try {
      return storeLocationSearchPort.search(command.query());
    } catch (StoreLocationSearchException exception) {
      if (exception.getType() == StoreLocationSearchException.Type.CONFIGURATION) {
        throw new BaseException(StoreErrorCode.STORE_LOCATION_SEARCH_CONFIGURATION_INVALID);
      }
      throw new BaseException(StoreErrorCode.STORE_LOCATION_SEARCH_UNAVAILABLE);
    }
  }
}
