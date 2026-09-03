package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.notice.result.StoreNoticeResult;
import io.point3.p3api.store.domain.type.StoreNoticeType;
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
        .map(notice -> new Notice(
            notice.type(),
            notice.items().stream()
                .map(item -> new Item(item.content(), item.sortOrder()))
                .toList()))
        .toList());
  }

  public record Notice(StoreNoticeType type, List<Item> items) {

    public Notice {
      items = List.copyOf(items);
    }

    @Override
    public List<Item> items() {
      return List.copyOf(items);
    }
  }

  public record Item(String content, int sortOrder) {}
}
