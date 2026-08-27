package io.point3.p3api.inquiry.application.submission.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.open.InquiryOpenService;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.inquiry.infrastructure.persistence.OrderFormSubmissionJpaRepository;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BuyerOrderFormSubmissionQueryServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private BuyerOrderFormSubmissionQueryService buyerOrderFormSubmissionQueryService;

  @Autowired
  private InquiryOpenService inquiryOpenService;

  @Autowired
  private OrderFormSubmissionJpaRepository orderFormSubmissionJpaRepository;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("구매자는 자신의 문의방에 제출한 주문서 상세를 조회한다")
  void getsOwnSubmission() {
    Fixture fixture = prepareFixture();

    OrderFormSubmission result = buyerOrderFormSubmissionQueryService.getSubmission(
        fixture.inquiry().getId(), fixture.submission().getId(), fixture.buyer().getId());

    assertEquals(fixture.submission().getId(), result.getId());
    assertEquals(fixture.buyer().getId(), result.getSubmittedBy());
    assertEquals("[{\"label\":\"메뉴명\",\"value\":\"초코 케이크\"}]", result.getAnswers());
  }

  @Test
  @DisplayName("다른 구매자는 제출 주문서를 조회할 수 없다")
  void rejectsOtherBuyer() {
    Fixture fixture = prepareFixture();
    User otherBuyer = saveUser(UserRole.BUYER, "other-buyer");

    BaseException exception = assertThrows(
        BaseException.class,
        () -> buyerOrderFormSubmissionQueryService.getSubmission(
            fixture.inquiry().getId(), fixture.submission().getId(), otherBuyer.getId()));

    assertEquals(ChatErrorCode.CHAT_PARTICIPANT_FORBIDDEN, exception.getErrorCode());
  }

  @Test
  @DisplayName("같은 구매자라도 다른 문의방의 제출 주문서는 조회할 수 없다")
  void rejectsSubmissionFromOtherInquiry() {
    Fixture fixture = prepareFixture();
    User otherSeller = saveUser(UserRole.SELLER, "other-seller");
    StoreResult otherStore = createStore(otherSeller.getId(), "다른 스토어");
    Inquiry otherInquiry = inquiryOpenService.open(
        OpenInquiryCommand.of(otherStore.id(), fixture.buyer().getId()));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> buyerOrderFormSubmissionQueryService.getSubmission(
            otherInquiry.getId(), fixture.submission().getId(), fixture.buyer().getId()));

    assertEquals(OrderFormErrorCode.ORDER_FORM_NOT_FOUND, exception.getErrorCode());
  }

  private Fixture prepareFixture() {
    User seller = saveUser(UserRole.SELLER, "seller");
    User buyer = saveUser(UserRole.BUYER, "buyer");
    StoreResult store = createStore(seller.getId(), "P3 베이커리");
    Inquiry inquiry = inquiryOpenService.open(OpenInquiryCommand.of(store.id(), buyer.getId()));
    OrderFormSubmission submission =
        orderFormSubmissionJpaRepository.saveAndFlush(OrderFormSubmission.create(
            inquiry.getId(),
            UUID.randomUUID(),
            buyer.getId(),
            LocalDate.parse("2026-08-30"),
            LocalTime.parse("13:30"),
            "[{\"label\":\"메뉴명\",\"value\":\"초코 케이크\"}]",
            "[]",
            true));

    return new Fixture(buyer, store, inquiry, submission);
  }

  private StoreResult createStore(UUID sellerId, String name) {
    return storeService.create(new CreateStoreCommand(
        sellerId,
        name,
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
      User buyer, StoreResult store, Inquiry inquiry, OrderFormSubmission submission) {}
}
