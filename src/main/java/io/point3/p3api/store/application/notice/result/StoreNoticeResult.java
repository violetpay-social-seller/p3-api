package io.point3.p3api.store.application.notice.result;

import io.point3.p3api.store.domain.entity.StoreNotice;
import io.point3.p3api.store.domain.type.StoreNoticeType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record StoreNoticeResult(List<Notice> notices) {

  public StoreNoticeResult {
    notices = List.copyOf(notices);
  }

  @Override
  public List<Notice> notices() {
    return List.copyOf(notices);
  }

  public static StoreNoticeResult from(List<StoreNotice> notices) {
    Map<StoreNoticeType, List<StoreNotice>> noticesByType =
        notices.stream().collect(java.util.stream.Collectors.groupingBy(StoreNotice::getType));
    return new StoreNoticeResult(List.of(StoreNoticeType.values()).stream()
        .map(type -> new Notice(
            type,
            noticesByType.getOrDefault(type, List.of()).stream()
                .sorted(Comparator.comparingInt(StoreNotice::getSortOrder))
                .map(notice -> new Item(notice.getContent(), notice.getSortOrder()))
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
