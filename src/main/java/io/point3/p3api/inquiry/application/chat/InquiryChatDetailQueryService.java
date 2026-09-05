package io.point3.p3api.inquiry.application.chat;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import io.point3.p3api.inquiry.application.startreference.OrderStartReferenceAssetService;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.user.application.port.UserPersistencePort;
import io.point3.p3api.user.domain.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryChatDetailQueryService implements InquiryChatDetailQueryUseCase {

  private final StorePersistencePort storePersistencePort;
  private final UserPersistencePort userPersistencePort;
  private final OrderStartReferenceAssetService orderStartReferenceAssetService;

  @Override
  public InquiryChatDetail getBuyerDetail(Inquiry inquiry) {
    Store store = findStore(inquiry);
    User owner = findUser(store.getOwnerUserId());

    return InquiryChatDetail.of(
        inquiry,
        store,
        new InquiryChatDetail.Participant(owner.getId(), owner.getName()),
        orderStartReferenceAssetService.findByInquiryId(inquiry.getId()),
        inquiry.getBuyerLastReadAt(),
        inquiry.getSellerLastReadAt());
  }

  @Override
  public InquiryChatDetail getSellerDetail(Inquiry inquiry) {
    Store store = findStore(inquiry);
    User buyer = findUser(inquiry.getBuyerUserId());

    return InquiryChatDetail.of(
        inquiry,
        store,
        new InquiryChatDetail.Participant(buyer.getId(), buyer.getName()),
        orderStartReferenceAssetService.findByInquiryId(inquiry.getId()),
        inquiry.getSellerLastReadAt(),
        inquiry.getBuyerLastReadAt());
  }

  private Store findStore(Inquiry inquiry) {
    return storePersistencePort
        .findById(inquiry.getStoreId())
        .orElseThrow(() -> new BaseException(ChatErrorCode.CHAT_INQUIRY_NOT_FOUND));
  }

  private User findUser(UUID userId) {
    return userPersistencePort
        .findById(userId)
        .orElseThrow(() -> new BaseException(ChatErrorCode.CHAT_INQUIRY_NOT_FOUND));
  }
}
