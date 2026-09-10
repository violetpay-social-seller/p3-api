package io.point3.p3api.inquiry.application.submission.revision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQuery;
import io.point3.p3api.chat.application.timeline.query.ChatTimelineQueryService;
import io.point3.p3api.chat.application.timeline.result.ChatTimelineItemResult;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.chat.infrastructure.persistence.ChatTimelineItemJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.command.RequestOrderFormRevisionCommand;
import io.point3.p3api.inquiry.application.list.InquiryListService;
import io.point3.p3api.inquiry.application.open.InquiryOpenService;
import io.point3.p3api.inquiry.application.result.InquiryListItem;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.inquiry.infrastructure.persistence.OrderFormSubmissionJpaRepository;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import io.point3.p3api.orderform.infrastructure.persistence.OrderFormTemplateJpaRepository;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderFormRevisionRequestServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private OrderFormRevisionRequestService orderFormRevisionRequestService;

  @Autowired
  private ChatTimelineQueryService chatTimelineQueryService;

  @Autowired
  private InquiryListService inquiryListService;

  @Autowired
  private InquiryOpenService inquiryOpenService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private OrderFormTemplateJpaRepository orderFormTemplateJpaRepository;

  @Autowired
  private OrderFormSubmissionJpaRepository orderFormSubmissionJpaRepository;

  @Autowired
  private ChatTimelineItemJpaRepository chatTimelineItemJpaRepository;

  @Test
  @DisplayName("판매자는 제출 주문서 수정 요청을 채팅 타임라인 이벤트로 남긴다")
  void requestsOrderFormRevision() {
    Fixture fixture = prepareFixture("form-revision");
    OrderFormSubmission submission = saveSubmission(fixture.inquiry(), fixture.buyer());

    ChatTimelineItemResult result = orderFormRevisionRequestService.requestRevision(
        RequestOrderFormRevisionCommand.of(
            fixture.inquiry().getId(),
            submission.getId(),
            fixture.store().id(),
            fixture.seller().getId()));

    assertEquals(submission.getId(), result.referenceId());
    assertEquals(ChatTimelineItemType.ORDER_FORM_REVISION_REQUEST, result.type());
    assertEquals(fixture.seller().getId(), result.senderUserId());
    assertNull(result.content());
    assertEquals(List.of(), result.assetIds());

    ChatTimelineItem persisted = chatTimelineItemJpaRepository
        .findById(result.eventId())
        .orElseThrow();
    assertEquals(fixture.inquiry().getId(), persisted.getInquiryId());
    assertEquals(submission.getId(), persisted.getReferenceId());

    ChatTimelinePage timelinePage = chatTimelineQueryService.execute(
        fixture.inquiry().getId(), new ChatTimelineQuery(null, null, 10));
    ChatTimelineItemResult timelineItem = timelinePage.items().getFirst();
    assertEquals(result.eventId(), timelineItem.eventId());
    assertEquals(ChatTimelineItemType.ORDER_FORM_REVISION_REQUEST, timelineItem.type());

    InquiryListItem buyerItem =
        inquiryListService.getBuyerInquiries(fixture.buyer().getId(), null).getFirst();
    InquiryListItem sellerItem =
        inquiryListService
            .getSellerInquiries(fixture.store().id(), fixture.seller().getId(), null)
            .getFirst();
    assertEquals(ChatTimelineItemType.ORDER_FORM_REVISION_REQUEST, buyerItem.latestEvent().type());
    assertEquals(submission.getId(), buyerItem.latestEvent().referenceId());
    assertEquals(1, buyerItem.unreadCount());
    assertEquals(ChatTimelineItemType.ORDER_FORM_REVISION_REQUEST, sellerItem.latestEvent().type());
    assertEquals(submission.getId(), sellerItem.latestEvent().referenceId());
    assertEquals(0, sellerItem.unreadCount());
  }

  @Test
  @DisplayName("판매자는 다른 스토어 문의의 주문서 수정을 요청할 수 없다")
  void rejectsOtherStoreInquiry() {
    Fixture fixture = prepareFixture("form-revision-other-store");
    OrderFormSubmission submission = saveSubmission(fixture.inquiry(), fixture.buyer());
    StoreResult otherStore = createStore(fixture.otherSeller().getId(), "other-store");

    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderFormRevisionRequestService.requestRevision(RequestOrderFormRevisionCommand.of(
            fixture.inquiry().getId(),
            submission.getId(),
            otherStore.id(),
            fixture.otherSeller().getId())));

    assertEquals(ChatErrorCode.CHAT_PARTICIPANT_FORBIDDEN, exception.getErrorCode());
  }

  @Test
  @DisplayName("판매자는 같은 스토어의 다른 문의 제출본으로 수정 요청할 수 없다")
  void rejectsOtherInquirySubmission() {
    Fixture fixture = prepareFixture("form-revision-other-inquiry");
    User otherBuyer = saveUser(UserRole.BUYER, "form-revision-other-buyer");
    Inquiry otherInquiry =
        inquiryOpenService.open(OpenInquiryCommand.of(fixture.store().id(), otherBuyer.getId()));
    OrderFormSubmission otherSubmission = saveSubmission(otherInquiry, otherBuyer);

    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderFormRevisionRequestService.requestRevision(RequestOrderFormRevisionCommand.of(
            fixture.inquiry().getId(),
            otherSubmission.getId(),
            fixture.store().id(),
            fixture.seller().getId())));

    assertEquals(OrderFormErrorCode.ORDER_FORM_NOT_FOUND, exception.getErrorCode());
  }

  private Fixture prepareFixture(String prefix) {
    User seller = saveUser(UserRole.SELLER, prefix + "-seller");
    User otherSeller = saveUser(UserRole.SELLER, prefix + "-other-seller");
    User buyer = saveUser(UserRole.BUYER, prefix + "-buyer");
    StoreResult store = createStore(seller.getId(), prefix + "-store");
    Inquiry inquiry = inquiryOpenService.open(OpenInquiryCommand.of(store.id(), buyer.getId()));
    return new Fixture(seller, otherSeller, buyer, store, inquiry);
  }

  private OrderFormSubmission saveSubmission(Inquiry inquiry, User buyer) {
    OrderFormTemplate template = orderFormTemplateJpaRepository.saveAndFlush(
        OrderFormTemplate.create(inquiry.getStoreId(), "기본 주문서"));
    return orderFormSubmissionJpaRepository.saveAndFlush(OrderFormSubmission.create(
        inquiry.getId(),
        template.getId(),
        buyer.getId(),
        LocalDate.parse("2030-08-30"),
        LocalTime.parse("13:30"),
        "[]",
        "[]",
        true));
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
    return userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail(prefix),
        prefix,
        role,
        "010-0000-0000",
        SignupProvider.GOOGLE));
  }

  private record Fixture(
      User seller, User otherSeller, User buyer, StoreResult store, Inquiry inquiry) {}
}
