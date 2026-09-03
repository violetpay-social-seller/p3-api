package io.point3.p3api.store.application.notice.command;

import io.point3.p3api.store.domain.type.StoreNoticeType;
import java.util.List;
import java.util.UUID;

public record UpdateStoreNoticesCommand(UUID storeId, List<Notice> notices) {

  public UpdateStoreNoticesCommand {
    if (notices != null) {
      notices = List.copyOf(notices);
    }
  }

  @Override
  public List<Notice> notices() {
    return notices == null ? null : List.copyOf(notices);
  }

  public record Notice(StoreNoticeType type, String content) {}
}
