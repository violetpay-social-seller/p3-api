package io.point3.p3api.store.notice.application.result;

import io.point3.p3api.store.notice.domain.entity.StoreNotice;
import io.point3.p3api.store.notice.domain.type.StoreNoticeType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record StoreNoticeResult(List<Notice> notices) {

  public StoreNoticeResult {
    notices = List.copyOf(notices);
  }

  @Override
  public List<Notice> notices() {
    return List.copyOf(notices);
  }

  public static StoreNoticeResult from(List<StoreNotice> notices) {
    Map<StoreNoticeType, StoreNotice> noticesByType = notices.stream()
        .collect(java.util.stream.Collectors.toMap(StoreNotice::getType, Function.identity()));
    return new StoreNoticeResult(List.of(StoreNoticeType.values()).stream()
        .map(type -> new Notice(
            type, noticesByType.containsKey(type) ? noticesByType.get(type).getContent() : null))
        .toList());
  }

  public record Notice(StoreNoticeType type, String content) {}
}
