package io.point3.p3api.store.application.notice.update;

import io.point3.p3api.store.application.notice.command.UpdateStoreNoticesCommand;
import io.point3.p3api.store.application.notice.result.StoreNoticeResult;

public interface StoreNoticeUpdateUseCase {

  StoreNoticeResult update(UpdateStoreNoticesCommand command);
}
