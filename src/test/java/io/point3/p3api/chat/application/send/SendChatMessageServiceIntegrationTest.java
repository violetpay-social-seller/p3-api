package io.point3.p3api.chat.application.send;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQueryService;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.chat.infrastructure.persistence.ChatMessageAssetJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.open.InquiryOpenService;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.notification.infrastructure.persistence.NotificationJpaRepository;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SendChatMessageServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private SendChatMessageService sendChatMessageService;

  @Autowired
  private ChatTimelineQueryService chatTimelineQueryService;

  @Autowired
  private InquiryOpenService inquiryOpenService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private ChatMessageAssetJpaRepository chatMessageAssetJpaRepository;

  @Autowired
  private NotificationJpaRepository notificationJpaRepository;

  @Test
  @DisplayName("이미지 메시지는 첨부 Asset 연결을 저장하고 타임라인에서 함께 조회된다")
  void sendsImageMessageWithAssets() {
    Fixture fixture = prepareFixture("chat-asset");
    Asset firstAsset = saveAsset(fixture.buyer().getId(), "first.png");
    Asset secondAsset = saveAsset(fixture.buyer().getId(), "second.png");

    SendChatMessageResult result = sendChatMessageService.execute(SendChatMessageCommand.of(
        fixture.inquiry().getId(),
        fixture.buyer().getId(),
        null,
        List.of(firstAsset.getId(), secondAsset.getId())));
    ChatTimelinePage page = chatTimelineQueryService.execute(
        fixture.inquiry().getId(), new ChatTimelineQuery(null, null, 10));

    assertNull(result.chatMessage().getContent());
    assertEquals(2, result.chatMessageAssets().size());
    assertEquals(2, chatMessageAssetJpaRepository.findAll().size());
    assertEquals(
        List.of(firstAsset.getId(), secondAsset.getId()), page.items().get(0).assetIds());
    assertEquals(
        0,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.seller().getId())
            .size());
    assertEquals(
        0,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.buyer().getId())
            .size());
  }

  @Test
  @DisplayName("다른 사용자가 업로드한 Asset은 채팅 메시지에 첨부할 수 없다")
  void rejectsOtherUserAsset() {
    Fixture fixture = prepareFixture("chat-other-asset");
    User otherBuyer = saveUser(UserRole.BUYER, "chat-other-asset-user");
    Asset otherAsset = saveAsset(otherBuyer.getId(), "other.png");

    BaseException exception = assertThrows(
        BaseException.class,
        () -> sendChatMessageService.execute(SendChatMessageCommand.of(
            fixture.inquiry().getId(),
            fixture.buyer().getId(),
            "참고 이미지",
            List.of(otherAsset.getId()))));

    assertEquals(AssetErrorCode.ASSET_NOT_FOUND, exception.getErrorCode());
  }

  private Fixture prepareFixture(String prefix) {
    User seller = saveUser(UserRole.SELLER, prefix + "-seller");
    User buyer = saveUser(UserRole.BUYER, prefix + "-buyer");
    StoreResult store = storeService.create(new CreateStoreCommand(
        seller.getId(),
        "P3 " + prefix,
        null,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        null,
        "{\"mon\":\"10:00-18:00\"}",
        "{\"leadTimeDays\":3}",
        "서울특별시 중구"));
    Inquiry inquiry = inquiryOpenService.open(OpenInquiryCommand.of(store.id(), buyer.getId()));
    return new Fixture(seller, buyer, store, inquiry);
  }

  private User saveUser(UserRole role, String prefix) {
    return userJpaRepository.saveAndFlush(
        User.create(UUID.randomUUID().toString(), uniqueEmail(prefix), prefix, role));
  }

  private Asset saveAsset(UUID uploadedBy, String filename) {
    return assetJpaRepository.saveAndFlush(Asset.create(
        UUID.randomUUID(),
        uploadedBy,
        filename,
        "image/png",
        1024,
        "original/" + UUID.randomUUID() + "/" + filename));
  }

  private record Fixture(User seller, User buyer, StoreResult store, Inquiry inquiry) {}
}
