package io.point3.p3api.inquiry.application.chat;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * buyer/seller가 inquiry 접근 가능한지 검증
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryChatAccessService {

  private final InquiryPersistencePort inquiryPersistencePort;

  public Inquiry getBuyerInquiry(UUID inquiryId, UUID buyerUserId) {
    Inquiry inquiry = getInquiry(inquiryId);

    if (!inquiry.getBuyerUserId().equals(buyerUserId)) {
      throw new BaseException(ChatErrorCode.CHAT_PARTICIPANT_FORBIDDEN);
    }

    return inquiry;
  }

  public Inquiry getSellerInquiry(UUID inquiryId, UUID storeId) {
    Inquiry inquiry = getInquiry(inquiryId);

    if (!inquiry.getStoreId().equals(storeId)) {
      throw new BaseException(ChatErrorCode.CHAT_PARTICIPANT_FORBIDDEN);
    }

    return inquiry;
  }

  private Inquiry getInquiry(UUID inquiryId) {
    return inquiryPersistencePort
        .findById(inquiryId)
        .orElseThrow(() -> new BaseException(ChatErrorCode.CHAT_INQUIRY_NOT_FOUND));
  }
}
