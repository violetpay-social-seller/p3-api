package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.result.StoreResult;

public record StoreShareLinkResponse(String slug, String url) {

  public static StoreShareLinkResponse from(StoreResult store, String url) {
    return new StoreShareLinkResponse(store.slug(), url);
  }
}
