package io.point3.p3api.store.application.location.port;

import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.util.List;

public interface StoreLocationSearchPort {

  List<StoreLocationResult> searchByKeyword(String query);

  List<StoreLocationResult> searchByAddress(String query);
}
