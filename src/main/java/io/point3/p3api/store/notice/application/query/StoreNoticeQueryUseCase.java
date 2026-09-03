package io.point3.p3api.store.notice.application.query;

import io.point3.p3api.store.notice.application.result.StoreNoticeResult;
import java.util.UUID;

public interface StoreNoticeQueryUseCase {

  StoreNoticeResult getNotices(UUID storeId);
}
