package io.point3.p3api.store.application.location.command;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;

public record SearchStoreLocationCommand(String query) {

  private static final int MIN_QUERY_LENGTH = 2;
  private static final int MAX_QUERY_LENGTH = 100;

  public static SearchStoreLocationCommand of(String query) {
    String normalized = query == null ? "" : query.trim();
    if (normalized.length() < MIN_QUERY_LENGTH || normalized.length() > MAX_QUERY_LENGTH) {
      throw new BaseException(StoreErrorCode.STORE_LOCATION_QUERY_INVALID);
    }
    return new SearchStoreLocationCommand(normalized);
  }
}
