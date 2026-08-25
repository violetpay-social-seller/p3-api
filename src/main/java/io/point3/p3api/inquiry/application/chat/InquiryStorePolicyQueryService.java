package io.point3.p3api.inquiry.application.chat;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.inquiry.application.result.InquiryStorePolicy;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryStorePolicyQueryService implements InquiryStorePolicyQueryUseCase {

  private final StorePersistencePort storePersistencePort;

  @Override
  public InquiryStorePolicy get(Inquiry inquiry) {
    Store store = storePersistencePort
        .findById(inquiry.getStoreId())
        .orElseThrow(() -> new BaseException(ChatErrorCode.CHAT_INQUIRY_NOT_FOUND));

    return InquiryStorePolicy.from(store);
  }
}
