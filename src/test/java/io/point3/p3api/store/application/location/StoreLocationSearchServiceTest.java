package io.point3.p3api.store.application.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.location.command.SearchStoreLocationCommand;
import io.point3.p3api.store.application.location.port.StoreLocationSearchException;
import io.point3.p3api.store.application.location.port.StoreLocationSearchPort;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class StoreLocationSearchServiceTest {

  @Test
  void trimsQueryBeforeSearching() {
    StoreLocationSearchPort port = query -> {
      assertEquals("테헤란로 123", query);
      return List.of(new StoreLocationResult("센트럴", "도로명", "지번", "06234"));
    };
    StoreLocationSearchService service = new StoreLocationSearchService(port);

    List<StoreLocationResult> results =
        service.search(SearchStoreLocationCommand.of("  테헤란로 123  "));

    assertEquals(1, results.size());
  }

  @Test
  void rejectsInvalidQuery() {
    BaseException blank =
        assertThrows(BaseException.class, () -> SearchStoreLocationCommand.of(" "));
    BaseException overlong =
        assertThrows(BaseException.class, () -> SearchStoreLocationCommand.of("a".repeat(101)));

    assertEquals(StoreErrorCode.STORE_LOCATION_QUERY_INVALID, blank.getErrorCode());
    assertEquals(StoreErrorCode.STORE_LOCATION_QUERY_INVALID, overlong.getErrorCode());
  }

  @Test
  void convertsExternalConfigurationFailure() {
    StoreLocationSearchPort port = query -> {
      throw new StoreLocationSearchException(StoreLocationSearchException.Type.CONFIGURATION);
    };
    StoreLocationSearchService service = new StoreLocationSearchService(port);

    BaseException exception = assertThrows(
        BaseException.class, () -> service.search(SearchStoreLocationCommand.of("테헤란로")));

    assertEquals(
        StoreErrorCode.STORE_LOCATION_SEARCH_CONFIGURATION_INVALID, exception.getErrorCode());
  }
}
