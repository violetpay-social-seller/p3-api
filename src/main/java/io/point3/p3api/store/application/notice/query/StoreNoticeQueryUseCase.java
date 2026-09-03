package io.point3.p3api.store.application.notice.query;

import io.point3.p3api.store.application.notice.result.StoreNoticeResult;
import java.util.UUID;

public interface StoreNoticeQueryUseCase {

  StoreNoticeResult getNotices(UUID storeId);
}
