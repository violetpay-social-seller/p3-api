package io.point3.p3api.chat.infrastructure.stomp;

import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.seller.provider.SellerStoreProvider;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** STOMP Principal이 지정한 문의방의 참여자인지 검증 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatStompParticipantAuthorizationService {

  private final CurrentUserRender currentUserRender;
  private final SellerStoreProvider sellerStoreProvider;
  private final InquiryChatAccessService inquiryChatAccessService;

  public CurrentUser requireParticipant(Authentication authentication, UUID inquiryId) {
    CurrentUser currentUser = currentUserRender.read(authentication);

    switch (currentUser.role()) {
      case BUYER -> inquiryChatAccessService.getBuyerInquiry(inquiryId, currentUser.userId());
      case SELLER ->
        inquiryChatAccessService.getSellerInquiry(
            inquiryId, sellerStoreProvider.resolveStoreId(currentUser));
      case OPERATOR -> throw new BaseException(CommonErrorCode.UNAUTHORIZED);
    }

    return currentUser;
  }
}
