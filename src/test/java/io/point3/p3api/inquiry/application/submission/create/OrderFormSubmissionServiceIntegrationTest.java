package io.point3.p3api.inquiry.application.submission.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.exception.code.GalleryErrorCode;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.gallery.application.GalleryItemService;
import io.point3.p3api.gallery.application.command.CreateGalleryItemCommand;
import io.point3.p3api.gallery.application.command.UpdateGalleryItemCommand;
import io.point3.p3api.gallery.application.result.GalleryItemResult;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.open.InquiryOpenService;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import io.point3.p3api.inquiry.infrastructure.persistence.OrderFormSubmissionJpaRepository;
import io.point3.p3api.orderform.application.OrderFormService;
import io.point3.p3api.orderform.application.create.CreateOrderFormCommand;
import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.orderform.domain.type.FieldType;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderFormSubmissionServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private OrderFormSubmissionService submissionService;

  @Autowired
  private OrderFormService orderFormService;

  @Autowired
  private InquiryOpenService inquiryOpenService;

  @Autowired
  private GalleryItemService galleryItemService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private StoreSettingService storeSettingService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private OrderFormSubmissionJpaRepository submissionJpaRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("주문서 제출은 활성 양식 검증 후 답변과 갤러리 참고 이미지를 스냅샷으로 저장한다")
  void createsSubmissionWithAnswerAndReferenceSnapshots() throws Exception {
    Fixture fixture = prepareFixture();
    OrderFormFieldResult nameField = fixture.form().fields().get(0);
    OrderFormFieldResult priceField = fixture.form().fields().get(1);

    OrderFormSubmission submission = submissionService.create(new CreateOrderFormSubmissionCommand(
        fixture.store().id(),
        fixture.buyer().getId(),
        fixture.inquiry().getId(),
        fixture.form().id(),
        List.of(
            new CreateOrderFormSubmissionCommand.FormAnswer(nameField.id(), textNode("초코 케이크")),
            new CreateOrderFormSubmissionCommand.FormAnswer(priceField.id(), numberNode(38000))),
        new CreateOrderFormSubmissionCommand.PickupRequest(
            LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
        new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
        new CreateOrderFormSubmissionCommand.CancellationRefundAgreement(true),
        List.of(new CreateOrderFormSubmissionCommand.ReferenceAsset(
            fixture.visibleGalleryAssetId(), OrderFormReferenceAssetSource.STORE_GALLERY, 0))));

    OrderFormSubmission persisted =
        submissionJpaRepository.findById(submission.getId()).orElseThrow();
    assertEquals(fixture.inquiry().getId(), persisted.getInquiryId());
    assertEquals(fixture.form().id(), persisted.getTemplateId());
    assertEquals(true, persisted.isCancellationRefundAgreed());
    JsonNode answers = objectMapper.readTree(persisted.getAnswers());
    assertEquals(2, answers.size());
    assertAnswerSnapshot(answers.get(0), nameField.id(), "메뉴명", "TEXT", true, 0, "초코 케이크");
    assertAnswerSnapshot(answers.get(1), priceField.id(), "기본 금액", "NUMBER", false, 1, 38000);

    JsonNode referenceAssets = objectMapper.readTree(persisted.getReferenceAssets());
    assertEquals(1, referenceAssets.size());
    assertEquals(
        fixture.visibleGalleryAssetId().toString(),
        referenceAssets.get(0).get("assetId").asText());
    assertEquals("STORE_GALLERY", referenceAssets.get(0).get("source").asText());
    assertEquals(0, referenceAssets.get(0).get("sortOrder").asInt());
  }

  @Test
  @DisplayName("주문 전 공지에 동의하지 않으면 주문서 제출을 거절한다")
  void rejectsSubmissionWithoutNoticeAgreement() {
    Fixture fixture = prepareFixture();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> submissionService.create(new CreateOrderFormSubmissionCommand(
            fixture.store().id(),
            fixture.buyer().getId(),
            fixture.inquiry().getId(),
            fixture.form().id(),
            List.of(new CreateOrderFormSubmissionCommand.FormAnswer(
                fixture.form().fields().get(0).id(), textNode("초코 케이크"))),
            new CreateOrderFormSubmissionCommand.PickupRequest(
                LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
            new CreateOrderFormSubmissionCommand.NoticeAgreement(false),
            CreateOrderFormSubmissionCommand.emptyReferenceAssets())));

    assertEquals(OrderFormErrorCode.ORDER_FORM_NOTICE_AGREEMENT_REQUIRED, exception.getErrorCode());
  }

  @Test
  @DisplayName("양식에 없는 필드 답변은 제출을 거절한다")
  void rejectsUnknownFieldAnswer() {
    Fixture fixture = prepareFixture();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> submissionService.create(new CreateOrderFormSubmissionCommand(
            fixture.store().id(),
            fixture.buyer().getId(),
            fixture.inquiry().getId(),
            fixture.form().id(),
            List.of(new CreateOrderFormSubmissionCommand.FormAnswer(
                UUID.randomUUID(), textNode("알 수 없는 답변"))),
            new CreateOrderFormSubmissionCommand.PickupRequest(
                LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
            new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
            CreateOrderFormSubmissionCommand.emptyReferenceAssets())));

    assertEquals(OrderFormErrorCode.ORDER_FORM_UNKNOWN_FIELD, exception.getErrorCode());
  }

  @Test
  @DisplayName("숨김 상태의 갤러리 이미지는 주문서 참고 이미지로 제출할 수 없다")
  void rejectsHiddenGalleryReferenceAsset() {
    Fixture fixture = prepareFixture();
    UUID hiddenAssetId = saveAsset(fixture.seller().getId(), "hidden-cake.png").getId();
    galleryItemService.create(
        new CreateGalleryItemCommand(fixture.store().id(), hiddenAssetId, 1, false));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> submissionService.create(new CreateOrderFormSubmissionCommand(
            fixture.store().id(),
            fixture.buyer().getId(),
            fixture.inquiry().getId(),
            fixture.form().id(),
            List.of(new CreateOrderFormSubmissionCommand.FormAnswer(
                fixture.form().fields().get(0).id(), textNode("초코 케이크"))),
            new CreateOrderFormSubmissionCommand.PickupRequest(
                LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
            new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
            List.of(new CreateOrderFormSubmissionCommand.ReferenceAsset(
                hiddenAssetId, OrderFormReferenceAssetSource.STORE_GALLERY, 0)))));

    assertEquals(GalleryErrorCode.GALLERY_ASSET_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("이미지 필드 Asset은 제출자의 업로드 파일만 허용한다")
  void validatesImageFieldAssetOwnership() {
    Fixture fixture = prepareFixture();
    OrderFormFieldResult imageField = fixture.form().fields().get(2);
    UUID buyerAssetId =
        saveAsset(fixture.buyer().getId(), "buyer-reference.png").getId();

    submissionService.create(new CreateOrderFormSubmissionCommand(
        fixture.store().id(),
        fixture.buyer().getId(),
        fixture.inquiry().getId(),
        fixture.form().id(),
        List.of(
            new CreateOrderFormSubmissionCommand.FormAnswer(
                fixture.form().fields().get(0).id(), textNode("초코 케이크")),
            new CreateOrderFormSubmissionCommand.FormAnswer(
                imageField.id(),
                objectMapper.getNodeFactory().arrayNode().add(buyerAssetId.toString()))),
        new CreateOrderFormSubmissionCommand.PickupRequest(
            LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
        new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
        CreateOrderFormSubmissionCommand.emptyReferenceAssets()));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> submissionService.create(new CreateOrderFormSubmissionCommand(
            fixture.store().id(),
            fixture.buyer().getId(),
            fixture.inquiry().getId(),
            fixture.form().id(),
            List.of(
                new CreateOrderFormSubmissionCommand.FormAnswer(
                    fixture.form().fields().get(0).id(), textNode("초코 케이크")),
                new CreateOrderFormSubmissionCommand.FormAnswer(
                    imageField.id(),
                    objectMapper
                        .getNodeFactory()
                        .arrayNode()
                        .add(fixture.visibleGalleryAssetId().toString()))),
            new CreateOrderFormSubmissionCommand.PickupRequest(
                LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
            new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
            CreateOrderFormSubmissionCommand.emptyReferenceAssets())));

    assertEquals(AssetErrorCode.ASSET_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("휴무일 픽업 요청은 영속 주문서 제출에서 다시 거절한다")
  void rejectsSubmissionWithHolidayPickup() {
    Fixture fixture = prepareFixture();
    storeSettingService.update(new UpdateStoreSettingCommand(
        fixture.store().id(),
        0,
        "주문 전 공지",
        0,
        java.util.Arrays.stream(DayOfWeek.values())
            .map(day -> new UpdateStoreSettingCommand.WeeklyPickupSetting(
                day, LocalTime.of(10, 0), LocalTime.of(18, 0), 10, true))
            .toList(),
        List.of(LocalDate.parse("2026-08-30"))));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> submissionService.create(new CreateOrderFormSubmissionCommand(
            fixture.store().id(),
            fixture.buyer().getId(),
            fixture.inquiry().getId(),
            fixture.form().id(),
            List.of(new CreateOrderFormSubmissionCommand.FormAnswer(
                fixture.form().fields().get(0).id(), textNode("초코 케이크"))),
            new CreateOrderFormSubmissionCommand.PickupRequest(
                LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
            new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
            CreateOrderFormSubmissionCommand.emptyReferenceAssets())));

    assertEquals(OrderFormErrorCode.ORDER_FORM_PICKUP_UNAVAILABLE, exception.getErrorCode());
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
            new CreateOrderFormCommand.Field("메뉴명", FieldType.TEXT, true, null, 0),
            new CreateOrderFormCommand.Field("기본 금액", FieldType.NUMBER, false, null, 1),
            new CreateOrderFormCommand.Field("참고 이미지", FieldType.IMAGE, false, null, 2))));
    savePickupSettings(store.id());
    Inquiry inquiry = inquiryOpenService.open(OpenInquiryCommand.of(store.id(), buyer.getId()));
    UUID visibleGalleryAssetId = createVisibleGalleryAsset(store.id(), seller.getId());

    return new Fixture(seller, buyer, store, form, inquiry, visibleGalleryAssetId);
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

  private UUID createVisibleGalleryAsset(UUID storeId, UUID sellerId) {
    UUID assetId = saveAsset(sellerId, "visible-cake.png").getId();
    GalleryItemResult galleryItem =
        galleryItemService.create(new CreateGalleryItemCommand(storeId, assetId, 0, false));
    galleryItemService.update(new UpdateGalleryItemCommand(
        storeId, galleryItem.id(), 0, false, StoreGalleryItemStatus.VISIBLE));
    return assetId;
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

  private Asset saveAsset(UUID uploadedBy, String filename) {
    return assetJpaRepository.saveAndFlush(Asset.create(
        UUID.randomUUID(),
        uploadedBy,
        filename,
        "image/png",
        1024,
        "original/" + UUID.randomUUID() + "/" + filename));
  }

  private JsonNode textNode(String value) {
    return objectMapper.getNodeFactory().textNode(value);
  }

  private JsonNode numberNode(int value) {
    return objectMapper.getNodeFactory().numberNode(value);
  }

  private void assertAnswerSnapshot(
      JsonNode snapshot,
      UUID fieldId,
      String label,
      String fieldType,
      boolean required,
      int sortOrder,
      String value) {
    assertEquals(fieldId.toString(), snapshot.get("fieldId").asText());
    assertEquals(label, snapshot.get("label").asText());
    assertEquals(fieldType, snapshot.get("fieldType").asText());
    assertEquals(required, snapshot.get("required").asBoolean());
    assertEquals(sortOrder, snapshot.get("sortOrder").asInt());
    assertEquals(value, snapshot.get("value").asText());
  }

  private void assertAnswerSnapshot(
      JsonNode snapshot,
      UUID fieldId,
      String label,
      String fieldType,
      boolean required,
      int sortOrder,
      int value) {
    assertEquals(fieldId.toString(), snapshot.get("fieldId").asText());
    assertEquals(label, snapshot.get("label").asText());
    assertEquals(fieldType, snapshot.get("fieldType").asText());
    assertEquals(required, snapshot.get("required").asBoolean());
    assertEquals(sortOrder, snapshot.get("sortOrder").asInt());
    assertEquals(value, snapshot.get("value").asInt());
  }

  private record Fixture(
      User seller,
      User buyer,
      StoreResult store,
      OrderFormResult form,
      Inquiry inquiry,
      UUID visibleGalleryAssetId) {}
}
