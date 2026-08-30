package io.point3.p3api.order.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.chat.infrastructure.persistence.ChatTimelineItemJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.open.InquiryOpenService;
import io.point3.p3api.inquiry.application.submission.create.OrderFormSubmissionService;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.notification.domain.type.NotificationType;
import io.point3.p3api.notification.infrastructure.persistence.NotificationJpaRepository;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.application.send.SendOrderConfirmationCommand;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import io.point3.p3api.order.infrastructure.persistence.OrderConfirmationJpaRepository;
import io.point3.p3api.orderform.application.OrderFormOptionCommand;
import io.point3.p3api.orderform.application.OrderFormService;
import io.point3.p3api.orderform.application.create.CreateOrderFormCommand;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.orderform.domain.type.OptionInputType;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import io.point3.p3api.orderform.domain.type.SelectionType;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.setting.StoreSettingService;
import io.point3.p3api.store.application.setting.command.UpdateStoreSettingCommand;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderConfirmationServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private OrderConfirmationService orderConfirmationService;

  @Autowired
  private OrderFormSubmissionService submissionService;

  @Autowired
  private OrderFormService orderFormService;

  @Autowired
  private InquiryOpenService inquiryOpenService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private StoreSettingService storeSettingService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private OrderConfirmationJpaRepository orderConfirmationJpaRepository;

  @Autowired
  private ChatTimelineItemJpaRepository chatTimelineItemJpaRepository;

  @Autowired
  private NotificationJpaRepository notificationJpaRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("주문확인서 전송은 스토어명과 주문서 스냅샷을 저장하고 타임라인 카드를 기록한다")
  void sendsOrderConfirmationWithSubmissionSnapshotAndTimelineItem() throws Exception {
    Fixture fixture = prepareFixture();

    SendOrderConfirmationResult result =
        orderConfirmationService.send(new SendOrderConfirmationCommand(
            fixture.inquiry().getId(),
            fixture.store().id(),
            fixture.seller().getId(),
            fixture.submission().getId(),
            "초코 케이크 1호",
            "초코 시트, 딸기 토핑",
            41000,
            Instant.parse("2026-08-30T04:30:00Z"),
            List.of(new SendOrderConfirmationCommand.AdditionalItem("토핑", "딸기", 3000L)),
            "픽업 10분 전에 연락 주세요."));

    OrderConfirmation persisted =
        orderConfirmationJpaRepository.findById(result.orderConfirmation().id()).orElseThrow();
    ChatTimelineItem timelineItem =
        chatTimelineItemJpaRepository.findById(result.chatTimelineItem().id()).orElseThrow();

    assertEquals(OrderConfirmationStatus.SENT, persisted.getStatus());
    assertNotNull(persisted.getSentAt());
    assertEquals("P3 베이커리", persisted.getStoreNameSnapshot());
    JsonNode orderSummary = objectMapper.readTree(persisted.getOrderSummary());
    assertEquals(
        fixture.submission().getId().toString(),
        orderSummary.get("orderFormSubmissionId").asText());
    JsonNode answers = orderSummary.get("answers");
    assertTrue(answers.isArray());
    assertEquals(2, answers.size());
    assertEquals("메뉴명", answers.get(0).get("label").asText());
    assertEquals("SINGLE", answers.get(0).get("selectionType").asText());
    assertEquals(
        "초코 케이크", answers.get(0).get("selectedOptions").get(0).get("text").asText());
    assertEquals(
        "10호", answers.get(1).get("selectedOptions").get(0).get("label").asText());
    assertEquals(
        "size-10", answers.get(1).get("selectedOptions").get(0).get("value").asText());
    assertEquals(
        38000, answers.get(1).get("selectedOptions").get(0).get("price").asLong());
    JsonNode additionalItems = objectMapper.readTree(persisted.getAdditionalItems());
    assertEquals(1, additionalItems.size());
    assertEquals("토핑", additionalItems.get(0).get("label").asText());
    assertEquals("딸기", additionalItems.get(0).get("value").asText());
    assertEquals(3000, additionalItems.get(0).get("amount").asInt());
    assertEquals(ChatTimelineItemType.ORDER_CONFIRMATION, timelineItem.getType());
    assertEquals(persisted.getId(), timelineItem.getReferenceId());
    assertEquals(
        1,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.seller().getId())
            .stream()
            .filter(notification -> notification.getType() == NotificationType.ORDER_FORM_SUBMITTED)
            .count());
    assertEquals(
        1,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.buyer().getId())
            .stream()
            .filter(
                notification -> notification.getType() == NotificationType.ORDER_CONFIRMATION_SENT)
            .count());
  }

  @Test
  @DisplayName("주문서와 주문확인서 재발행은 수정 알림을 저장한다")
  void notifiesUpdatesForResubmissionAndConfirmationReplacement() {
    Fixture fixture = prepareFixture();
    submitOrderForm(
        fixture.store().id(), fixture.buyer().getId(), fixture.inquiry(), fixture.form());

    SendOrderConfirmationCommand command = new SendOrderConfirmationCommand(
        fixture.inquiry().getId(),
        fixture.store().id(),
        fixture.seller().getId(),
        fixture.submission().getId(),
        "초코 케이크 1호",
        "초코 시트",
        38000,
        Instant.parse("2026-08-30T04:30:00Z"),
        List.of(),
        null);
    orderConfirmationService.send(command);
    orderConfirmationService.send(command);

    assertEquals(
        1,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.seller().getId())
            .stream()
            .filter(notification -> notification.getType() == NotificationType.ORDER_FORM_UPDATED)
            .count());
    assertEquals(
        1,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.buyer().getId())
            .stream()
            .filter(notification ->
                notification.getType() == NotificationType.ORDER_CONFIRMATION_UPDATED)
            .count());
  }

  @Test
  @DisplayName("다른 문의방의 제출 주문서를 사용한 주문확인서 전송은 거절한다")
  void rejectsOrderConfirmationForSubmissionFromAnotherInquiry() {
    Fixture fixture = prepareFixture();
    User anotherBuyer = saveUser(UserRole.BUYER, "buyer-other");
    Inquiry anotherInquiry =
        inquiryOpenService.open(OpenInquiryCommand.of(fixture.store().id(), anotherBuyer.getId()));
    OrderFormSubmission anotherSubmission =
        submitOrderForm(fixture.store().id(), anotherBuyer.getId(), anotherInquiry, fixture.form());

    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderConfirmationService.send(new SendOrderConfirmationCommand(
            fixture.inquiry().getId(),
            fixture.store().id(),
            fixture.seller().getId(),
            anotherSubmission.getId(),
            "초코 케이크 1호",
            "초코 시트",
            38000,
            Instant.parse("2026-08-30T04:30:00Z"),
            List.of(),
            null)));

    assertEquals(
        OrderConfirmationErrorCode.ORDER_CONFIRMATION_SUBMISSION_INVALID, exception.getErrorCode());
  }

  private Fixture prepareFixture() {
    User seller = saveUser(UserRole.SELLER, "seller");
    User buyer = saveUser(UserRole.BUYER, "buyer");
    StoreResult store = storeService.create(new CreateStoreCommand(
        seller.getId(),
        "P3 베이커리",
        null,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        null,
        "{\"mon\":\"10:00-18:00\"}",
        "{\"leadTimeDays\":3}",
        "서울특별시 중구"));
    OrderFormResult form = orderFormService.create(new CreateOrderFormCommand(
        store.id(),
        "주문서",
        List.of(
            optionGroup("메뉴명", true, 0, option("메뉴명", "menu", OptionInputType.TEXT, 0L, 0)),
            optionGroup(
                "사이즈", true, 1, option("10호", "size-10", OptionInputType.SELECT, 38000L, 0)))));
    savePickupSettings(store.id());
    Inquiry inquiry = inquiryOpenService.open(OpenInquiryCommand.of(store.id(), buyer.getId()));
    OrderFormSubmission submission = submitOrderForm(store.id(), buyer.getId(), inquiry, form);

    return new Fixture(seller, buyer, store, form, inquiry, submission);
  }

  private OrderFormSubmission submitOrderForm(
      UUID storeId, UUID buyerUserId, Inquiry inquiry, OrderFormResult form) {
    return submissionService.create(new CreateOrderFormSubmissionCommand(
        storeId,
        buyerUserId,
        inquiry.getId(),
        form.id(),
        List.of(
            new CreateOrderFormSubmissionCommand.FormAnswer(
                form.optionGroups().get(0).id(),
                selections(selection("menu").put("text", "초코 케이크"))),
            new CreateOrderFormSubmissionCommand.FormAnswer(
                form.optionGroups().get(1).id(), selections(selection("size-10")))),
        new CreateOrderFormSubmissionCommand.PickupRequest(
            LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
        new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
        CreateOrderFormSubmissionCommand.emptyReferenceAssets()));
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

  private CreateOrderFormCommand.OptionGroup optionGroup(
      String label, boolean required, int sortOrder, OrderFormOptionCommand option) {
    return new CreateOrderFormCommand.OptionGroup(
        label,
        SelectionType.SINGLE,
        required,
        sortOrder,
        OrderFormCategory.DESIGN,
        OrderFormCategory.DESIGN.getTitle(),
        null,
        0,
        List.of(option));
  }

  private OrderFormOptionCommand option(
      String label, String value, OptionInputType inputType, Long price, int sortOrder) {
    return new OrderFormOptionCommand(label, value, inputType, price, null, true, sortOrder);
  }

  private com.fasterxml.jackson.databind.node.ObjectNode selection(String optionValue) {
    return objectMapper.getNodeFactory().objectNode().put("optionValue", optionValue);
  }

  private com.fasterxml.jackson.databind.node.ArrayNode selections(JsonNode... selectedOptions) {
    com.fasterxml.jackson.databind.node.ArrayNode selections =
        objectMapper.getNodeFactory().arrayNode();
    for (JsonNode selectedOption : selectedOptions) {
      selections.add(selectedOption);
    }
    return selections;
  }

  private void savePickupSettings(UUID storeId) {
    storeSettingService.update(new UpdateStoreSettingCommand(
        storeId,
        0,
        "주문 전 공지",
        0,
        java.util.Arrays.stream(DayOfWeek.values())
            .map(day -> new UpdateStoreSettingCommand.WeeklyPickupSetting(
                day, LocalTime.of(10, 0), LocalTime.of(18, 0), 10, true))
            .toList(),
        List.of()));
  }

  private record Fixture(
      User seller,
      User buyer,
      StoreResult store,
      OrderFormResult form,
      Inquiry inquiry,
      OrderFormSubmission submission) {}
}
