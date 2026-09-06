package io.point3.p3api.store.application.location;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.location.command.SearchStoreLocationCommand;
import io.point3.p3api.store.application.location.port.StoreLocationSearchException;
import io.point3.p3api.store.application.location.port.StoreLocationSearchPort;
import io.point3.p3api.store.application.location.query.StoreLocationSearchUseCase;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreLocationSearchService implements StoreLocationSearchUseCase {

  private static final Pattern ADDRESS_QUERY_PATTERN =
      Pattern.compile(".*(?:[로길]\\s*\\d+|[동읍면리]\\s*\\d+(?:-\\d+)?).*");

  private final StoreLocationSearchPort storeLocationSearchPort;

  @Override
  public List<StoreLocationResult> search(SearchStoreLocationCommand command) {
    try {
      List<StoreLocationResult> keywordResults =
          storeLocationSearchPort.searchByKeyword(command.query());
      if (!isAddressQuery(command.query())) {
        return keywordResults;
      }

      List<StoreLocationResult> addressResults =
          storeLocationSearchPort.searchByAddress(command.query());
      return merge(keywordResults, addressResults);
    } catch (StoreLocationSearchException exception) {
      throw toBaseException(exception);
    }
  }

  private boolean isAddressQuery(String query) {
    return ADDRESS_QUERY_PATTERN.matcher(query).matches();
  }

  private List<StoreLocationResult> merge(
      List<StoreLocationResult> keywordResults, List<StoreLocationResult> addressResults) {
    Map<String, StoreLocationResult> results = new LinkedHashMap<>();
    keywordResults.forEach(result -> results.putIfAbsent(key(result), result));
    addressResults.forEach(result -> results.putIfAbsent(key(result), result));
    return List.copyOf(results.values());
  }

  private String key(StoreLocationResult result) {
    return String.join(
        "|",
        result.name(),
        nullToEmpty(result.roadAddress()),
        nullToEmpty(result.jibunAddress()),
        String.valueOf(result.latitude()),
        String.valueOf(result.longitude()));
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private BaseException toBaseException(StoreLocationSearchException exception) {
    if (exception.getType() == StoreLocationSearchException.Type.CONFIGURATION) {
      return new BaseException(StoreErrorCode.STORE_LOCATION_SEARCH_CONFIGURATION_INVALID);
    }
    return new BaseException(StoreErrorCode.STORE_LOCATION_SEARCH_UNAVAILABLE);
  }
}
