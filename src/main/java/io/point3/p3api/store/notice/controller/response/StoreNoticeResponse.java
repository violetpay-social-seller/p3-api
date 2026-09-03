package io.point3.p3api.store.notice.controller.response;

import io.point3.p3api.store.notice.application.result.StoreNoticeResult;
import io.point3.p3api.store.notice.domain.type.StoreNoticeType;
import java.util.List;

public record StoreNoticeResponse(List<Notice> notices) {

  public StoreNoticeResponse {
    notices = List.copyOf(notices);
  }

  @Override
  public List<Notice> notices() {
    return List.copyOf(notices);
  }

  public static StoreNoticeResponse from(StoreNoticeResult result) {
    return new StoreNoticeResponse(result.notices().stream()
        .map(notice -> new Notice(notice.type(), notice.content()))
        .toList());
  }

  public record Notice(StoreNoticeType type, String content) {}
}
