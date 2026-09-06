package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.util.List;

public record StoreLocationSearchResponse(List<Item> items) {

  public StoreLocationSearchResponse {
    items = List.copyOf(items);
  }

  public static StoreLocationSearchResponse from(List<StoreLocationResult> results) {
    return new StoreLocationSearchResponse(results.stream().map(Item::from).toList());
  }

  @Override
  public List<Item> items() {
    return List.copyOf(items);
  }

  public record Item(String buildingName, String roadAddress, String jibunAddress, String zipCode) {

    private static Item from(StoreLocationResult result) {
      return new Item(
          result.buildingName(), result.roadAddress(), result.jibunAddress(), result.zipCode());
    }
  }
}
