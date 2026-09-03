package io.point3.p3api.store.controller.request;

import io.point3.p3api.store.application.notice.command.UpdateStoreNoticesCommand;
import io.point3.p3api.store.domain.type.StoreNoticeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record StoreNoticeUpdateRequest(
    @NotNull @Size(min = 5, max = 5) @Valid List<Notice> notices) {

  public StoreNoticeUpdateRequest {
    notices = notices == null ? null : List.copyOf(notices);
  }

  @Override
  public List<Notice> notices() {
    return notices == null ? null : List.copyOf(notices);
  }

  public UpdateStoreNoticesCommand toCommand(UUID storeId) {
    return new UpdateStoreNoticesCommand(
        storeId,
        notices.stream()
            .map(notice -> new UpdateStoreNoticesCommand.Notice(notice.type(), notice.content()))
            .toList());
  }

  public record Notice(@NotNull StoreNoticeType type, String content) {}
}
