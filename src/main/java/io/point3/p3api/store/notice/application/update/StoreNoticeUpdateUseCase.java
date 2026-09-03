package io.point3.p3api.store.notice.application.update;

import io.point3.p3api.store.notice.application.command.UpdateStoreNoticesCommand;
import io.point3.p3api.store.notice.application.result.StoreNoticeResult;

public interface StoreNoticeUpdateUseCase {

  StoreNoticeResult update(UpdateStoreNoticesCommand command);
}
