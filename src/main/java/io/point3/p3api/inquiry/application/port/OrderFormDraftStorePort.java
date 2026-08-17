package io.point3.p3api.inquiry.application.port;

import io.point3.p3api.inquiry.application.draft.model.OrderFormDraftData;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import java.util.Optional;

public interface OrderFormDraftStorePort {

  OrderFormDraftResult save(OrderFormDraftData draftData);

  Optional<OrderFormDraftData> findByDraftKey(String draftKey);

  void delete(String draftKey);
}
