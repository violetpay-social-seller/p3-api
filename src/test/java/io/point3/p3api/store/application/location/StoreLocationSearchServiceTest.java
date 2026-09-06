package io.point3.p3api.store.application.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.location.command.SearchStoreLocationCommand;
import io.point3.p3api.store.application.location.port.StoreLocationSearchException;
import io.point3.p3api.store.application.location.port.StoreLocationSearchPort;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StoreLocationSearchServiceTest {

  @Test
  void searchesKeywordOnlyForGeneralQuery() {
    RecordingSearchPort port = new RecordingSearchPort();
    port.keywordResults = List.of(location("강남역 2호선"));
    StoreLocationSearchService service = new StoreLocationSearchService(port);

    List<StoreLocationResult> results = service.search(SearchStoreLocationCommand.of("  강남역  "));

    assertEquals(List.of(location("강남역 2호선")), results);
    assertEquals(List.of("강남역"), port.keywordQueries);
    assertTrue(port.addressQueries.isEmpty());
  }

  @Test
  void supplementsAddressQueryAndRemovesDuplicates() {
    RecordingSearchPort port = new RecordingSearchPort();
    StoreLocationResult duplicated = location("테헤란로 123");
    port.keywordResults = List.of(duplicated);
    port.addressResults = List.of(duplicated, location("테헤란로 125"));
    StoreLocationSearchService service = new StoreLocationSearchService(port);

    List<StoreLocationResult> results = service.search(SearchStoreLocationCommand.of("테헤란로 123"));

    assertEquals(List.of(duplicated, location("테헤란로 125")), results);
    assertEquals(List.of("테헤란로 123"), port.keywordQueries);
    assertEquals(List.of("테헤란로 123"), port.addressQueries);
  }

  @Test
  void rejectsBlankAndOverlongQuery() {
    assertThrows(BaseException.class, () -> SearchStoreLocationCommand.of(" "));
    BaseException exception =
        assertThrows(BaseException.class, () -> SearchStoreLocationCommand.of("a".repeat(101)));

    assertEquals(StoreErrorCode.STORE_LOCATION_QUERY_INVALID, exception.getErrorCode());
  }

  @Test
  void convertsExternalConfigurationFailure() {
    RecordingSearchPort port = new RecordingSearchPort();
    port.exception =
        new StoreLocationSearchException(StoreLocationSearchException.Type.CONFIGURATION);
    StoreLocationSearchService service = new StoreLocationSearchService(port);

    BaseException exception = assertThrows(
        BaseException.class, () -> service.search(SearchStoreLocationCommand.of("강남역")));

    assertEquals(
        StoreErrorCode.STORE_LOCATION_SEARCH_CONFIGURATION_INVALID, exception.getErrorCode());
  }

  private StoreLocationResult location(String name) {
    return new StoreLocationResult(name, "서울 강남구 테헤란로 123", "서울 강남구 역삼동 123", 37.5, 127.0);
  }

  private static class RecordingSearchPort implements StoreLocationSearchPort {

    private final List<String> keywordQueries = new ArrayList<>();
    private final List<String> addressQueries = new ArrayList<>();
    private List<StoreLocationResult> keywordResults = List.of();
    private List<StoreLocationResult> addressResults = List.of();
    private StoreLocationSearchException exception;

    @Override
    public List<StoreLocationResult> searchByKeyword(String query) {
      keywordQueries.add(query);
      throwIfPresent();
      return keywordResults;
    }

    @Override
    public List<StoreLocationResult> searchByAddress(String query) {
      addressQueries.add(query);
      throwIfPresent();
      return addressResults;
    }

    private void throwIfPresent() {
      if (exception != null) {
        throw exception;
      }
    }
  }
}
