package io.point3.p3api.inquiry.application.submission.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import io.point3.p3api.assetvariant.infrastructure.persistence.AssetVariantJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.open.InquiryOpenService;
import io.point3.p3api.inquiry.application.submission.result.OrderFormSubmissionResult;
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
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "p3.asset.delivery.base-url=https://assets.example.test")
class BuyerOrderFormSubmissionQueryServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private BuyerOrderFormSubmissionQueryService buyerOrderFormSubmissionQueryService;

  @Autowired
  private InquiryOpenService inquiryOpenService;

  @Autowired
  private OrderFormSubmissionJpaRepository orderFormSubmissionJpaRepository;

  @Autowired
  private OrderFormTemplateJpaRepository orderFormTemplateJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private AssetVariantJpaRepository assetVariantJpaRepository;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("구매자는 자신의 문의방에 제출한 주문서 상세를 조회한다")
  void getsOwnSubmission() {
    Fixture fixture = prepareFixture();

    OrderFormSubmissionResult result = buyerOrderFormSubmissionQueryService.getSubmission(
        fixture.inquiry().getId(), fixture.submission().getId(), fixture.buyer().getId());

    assertEquals(fixture.submission().getId(), result.id());
    assertEquals(fixture.buyer().getId(), result.submittedBy());
    assertEquals("[{\"label\":\"메뉴명\",\"value\":\"초코 케이크\"}]", result.answers());
  }

  @Test
  @DisplayName("이미지 답변은 조회 응답에 delivery URL을 포함한다")
  void getsSubmissionWithImageAnswerDelivery() throws Exception {
    Fixture fixture = prepareFixture();
    Asset readyAsset = saveAsset(fixture.buyer().getId(), "buyer-reference.png");
    Asset pendingAsset = saveAsset(fixture.buyer().getId(), "pending-reference.png");
    saveVariant(readyAsset, "processed/buyer-reference_640.webp");
    String answers = ("[{\"label\":\"참고 이미지\","
            + "\"value\":[{\"optionValue\":\"reference\",\"assetIds\":[\"%s\",\"%s\"]}],"
            + "\"selectedOptions\":[{\"value\":\"reference\",\"assetIds\":[\"%s\",\"%s\"]}]}]")
        .formatted(
            readyAsset.getId(), pendingAsset.getId(), readyAsset.getId(), pendingAsset.getId());
    OrderFormSubmission submission =
        orderFormSubmissionJpaRepository.saveAndFlush(OrderFormSubmission.create(
            fixture.inquiry().getId(),
            fixture.template().getId(),
            fixture.buyer().getId(),
            LocalDate.parse("2026-08-30"),
            LocalTime.parse("13:30"),
            answers,
            "[]",
            true));

    OrderFormSubmissionResult result = buyerOrderFormSubmissionQueryService.getSubmission(
        fixture.inquiry().getId(), submission.getId(), fixture.buyer().getId());

    JsonNode answer = objectMapper.readTree(result.answers()).get(0);
    assertImageAsset(answer.get("value").get(0));
    assertImageAsset(answer.get("selectedOptions").get(0));
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
    OrderFormTemplate template = createTemplate(store.id());
    Inquiry inquiry = inquiryOpenService.open(OpenInquiryCommand.of(store.id(), buyer.getId()));
    OrderFormSubmission submission =
        orderFormSubmissionJpaRepository.saveAndFlush(OrderFormSubmission.create(
            inquiry.getId(),
            template.getId(),
            buyer.getId(),
            LocalDate.parse("2026-08-30"),
            LocalTime.parse("13:30"),
            "[{\"label\":\"메뉴명\",\"value\":\"초코 케이크\"}]",
            "[]",
            true));

    return new Fixture(buyer, store, template, inquiry, submission);
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

  private OrderFormTemplate createTemplate(UUID storeId) {
    return orderFormTemplateJpaRepository.saveAndFlush(OrderFormTemplate.create(storeId, "기본 주문서"));
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

  private void saveVariant(Asset asset, String objectKey) {
    assetVariantJpaRepository.saveAndFlush(AssetVariant.create(
        asset, AssetVariantType.MEDIUM, objectKey, "image/webp", 640, 640, 512));
  }

  private void assertImageAsset(JsonNode selectedOption) {
    JsonNode assets = selectedOption.get("assets");
    JsonNode asset = assets.get(0);
    assertEquals(
        "https://assets.example.test/processed/buyer-reference_640.webp",
        asset.get("deliveryUrl").asText());
    assertEquals("MEDIUM", asset.get("variants").get(0).get("type").asText());
    assertEquals(
        "https://assets.example.test/processed/buyer-reference_640.webp",
        asset.get("variants").get(0).get("deliveryUrl").asText());
    assertTrue(assets.get(1).get("deliveryUrl").isNull());
    assertEquals(0, assets.get(1).get("variants").size());
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
      User buyer,
      StoreResult store,
      OrderFormTemplate template,
      Inquiry inquiry,
      OrderFormSubmission submission) {}
}
