package io.point3.p3api.inquiry.application.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.chat.application.timeline.ChatTimelineItemPublisher;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.open.InquiryOpenService;
import io.point3.p3api.inquiry.application.result.InquiryListItem;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import io.point3.p3api.inquiry.infrastructure.persistence.InquiryJpaRepository;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

class InquiryListServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private InquiryListService inquiryListService;

  @Autowired
  private InquiryChatAccessService inquiryChatAccessService;

  @Autowired
  private InquiryOpenService inquiryOpenService;

  @Autowired
  private ChatTimelineItemPublisher chatTimelineItemPublisher;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private InquiryJpaRepository inquiryJpaRepository;

  @Test
  @DisplayName("상담 목록은 읽지 않음과 사용자별 휴지통 상태를 기준으로 필터링한다")
  void filtersUnreadAndParticipantTrash() {
    Fixture fixture = prepareFixture("inquiry-list");
    chatTimelineItemPublisher.publishMessage(
        fixture.firstInquiry().getId(), fixture.firstSeller().getId(), UUID.randomUUID());

    List<InquiryListItem> unreadItems =
        inquiryListService.getBuyerInquiries(fixture.buyer().getId(), null, true);
    inquiryListService.moveBuyerToTrash(
        fixture.firstInquiry().getId(), fixture.buyer().getId());
    List<InquiryListItem> activeBuyerItems =
        inquiryListService.getBuyerInquiries(fixture.buyer().getId(), null);
    List<InquiryListItem> buyerTrashItems =
        inquiryListService.getBuyerInquiries(fixture.buyer().getId(), InquiryStatus.TRASH);
    List<InquiryListItem> sellerItems = inquiryListService.getSellerInquiries(
        fixture.firstStore().id(), fixture.firstSeller().getId(), null);

    assertEquals(1, unreadItems.size());
    assertEquals(fixture.firstInquiry().getId(), unreadItems.get(0).inquiryId());
    assertEquals(1, activeBuyerItems.size());
    assertEquals(fixture.secondInquiry().getId(), activeBuyerItems.get(0).inquiryId());
    assertEquals(1, buyerTrashItems.size());
    assertEquals(InquiryStatus.TRASH, buyerTrashItems.get(0).status());
    assertEquals(1, sellerItems.size());
    assertEquals(fixture.firstInquiry().getId(), sellerItems.get(0).inquiryId());

    inquiryListService.restoreBuyerFromTrash(
        fixture.firstInquiry().getId(), fixture.buyer().getId());

    assertEquals(
        2, inquiryListService.getBuyerInquiries(fixture.buyer().getId(), null).size());
  }

  @Test
  @DisplayName("휴지통 이동 1개월이 지난 상담은 해당 사용자 목록에서 비운다")
  void purgesExpiredTrashForParticipant() {
    Fixture fixture = prepareFixture("inquiry-trash-purge");
    fixture.firstInquiry().moveBuyerToTrash(Instant.parse("2026-07-20T00:00:00Z"));
    inquiryJpaRepository.saveAndFlush(fixture.firstInquiry());

    inquiryListService.purgeExpiredTrash();
    BaseException exception = assertThrows(
        BaseException.class,
        () -> inquiryChatAccessService.getBuyerInquiry(
            fixture.firstInquiry().getId(), fixture.buyer().getId()));

    assertEquals(ChatErrorCode.CHAT_INQUIRY_NOT_FOUND, exception.getErrorCode());
    assertEquals(
        0,
        inquiryListService
            .getBuyerInquiries(fixture.buyer().getId(), InquiryStatus.TRASH)
            .size());
    assertEquals(
        1,
        inquiryListService
            .getSellerInquiries(fixture.firstStore().id(), fixture.firstSeller().getId(), null)
            .size());
  }

  @Test
  @DisplayName("판매자는 다른 스토어 문의방에 접근할 수 없다")
  void rejectsOtherStoreInquiryAccess() {
    Fixture fixture = prepareFixture("inquiry-other-store");

    BaseException exception = assertThrows(
        BaseException.class,
        () -> inquiryChatAccessService.getSellerInquiry(
            fixture.firstInquiry().getId(), fixture.secondStore().id()));

    assertEquals(ChatErrorCode.CHAT_PARTICIPANT_FORBIDDEN, exception.getErrorCode());
  }

  private Fixture prepareFixture(String prefix) {
    User firstSeller = saveUser(UserRole.SELLER, prefix + "-seller1");
    User secondSeller = saveUser(UserRole.SELLER, prefix + "-seller2");
    User buyer = saveUser(UserRole.BUYER, prefix + "-buyer");
    StoreResult firstStore = createStore(firstSeller.getId(), prefix + " first");
    StoreResult secondStore = createStore(secondSeller.getId(), prefix + " second");
    Inquiry firstInquiry =
        inquiryOpenService.open(OpenInquiryCommand.of(firstStore.id(), buyer.getId()));
    Inquiry secondInquiry =
        inquiryOpenService.open(OpenInquiryCommand.of(secondStore.id(), buyer.getId()));

    return new Fixture(
        firstSeller, secondSeller, buyer, firstStore, secondStore, firstInquiry, secondInquiry);
  }

  private StoreResult createStore(UUID ownerUserId, String name) {
    return storeService.create(new CreateStoreCommand(
        ownerUserId,
        "P3 " + name,
        null,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        null,
        "{\"mon\":\"10:00-18:00\"}",
        "{\"leadTimeDays\":3}",
        "서울특별시 중구"));
  }

  private User saveUser(UserRole role, String prefix) {
    return userJpaRepository.saveAndFlush(
        User.create(UUID.randomUUID().toString(), uniqueEmail(prefix), prefix, role));
  }

  private record Fixture(
      User firstSeller,
      User secondSeller,
      User buyer,
      StoreResult firstStore,
      StoreResult secondStore,
      Inquiry firstInquiry,
      Inquiry secondInquiry) {}

  @TestConfiguration
  static class InquiryListClockTestConfiguration {

    @Bean
    @Primary
    Clock fixedInquiryListClock() {
      return Clock.fixed(Instant.parse("2026-08-25T03:00:00Z"), ZoneOffset.UTC);
    }
  }
}
