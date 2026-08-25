package io.point3.p3api.orderform.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.orderform.application.create.CreateOrderFormCommand;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.orderform.application.update.UpdateOrderFormCommand;
import io.point3.p3api.orderform.domain.type.FieldType;
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

class OrderFormServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private OrderFormService orderFormService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("스토어에는 활성 주문서 양식을 하나만 만들 수 있다")
  void rejectsDuplicateActiveTemplate() {
    StoreResult store = createStore();
    orderFormService.create(createCommand(store.id(), "기본 주문서"));

    BaseException exception = assertThrows(
        BaseException.class, () -> orderFormService.create(createCommand(store.id(), "추가 주문서")));

    assertEquals(OrderFormErrorCode.ORDER_FORM_ACTIVE_ALREADY_EXISTS, exception.getErrorCode());
  }

  @Test
  @DisplayName("필드 sortOrder가 0부터 연속되지 않으면 주문서 양식을 생성할 수 없다")
  void rejectsNonSequentialFieldSortOrder() {
    StoreResult store = createStore();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.Field("메뉴명", FieldType.TEXT, true, null, 1)))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("필드 settings는 JSON object만 허용한다")
  void rejectsInvalidFieldSettings() {
    StoreResult store = createStore();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.Field(
                "픽업 희망일", FieldType.DATE, true, "\"not-object\"", 0)))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("주문서 수정은 기존 필드를 교체하고 비활성화하면 활성 양식 조회에서 제외한다")
  void updatesAndInactivatesTemplate() {
    StoreResult store = createStore();
    OrderFormResult created = orderFormService.create(createCommand(store.id(), "기본 주문서"));

    OrderFormResult updated = orderFormService.update(new UpdateOrderFormCommand(
        store.id(),
        created.id(),
        "수정 주문서",
        List.of(
            new UpdateOrderFormCommand.Field("메뉴명", FieldType.TEXT, true, null, 0),
            new UpdateOrderFormCommand.Field("요청사항", FieldType.TEXTAREA, false, null, 1))));
    OrderFormResult inactive = orderFormService.inactive(store.id(), created.id());
    BaseException exception =
        assertThrows(BaseException.class, () -> orderFormService.getActiveTemplate(store.id()));

    assertEquals("수정 주문서", updated.name());
    assertEquals(2, updated.fields().size());
    assertFalse(inactive.active());
    assertEquals(OrderFormErrorCode.ORDER_FORM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("그룹과 선택지를 포함한 주문서 정의를 순서대로 저장하고 수정할 수 있다")
  void savesGroupsFieldsAndOptions() {
    StoreResult store = createStore();
    OrderFormResult created = orderFormService.create(new CreateOrderFormCommand(
        store.id(),
        "주문서",
        List.of(
            new CreateOrderFormCommand.Field(
                "케이크 크기",
                FieldType.SINGLE_SELECT,
                true,
                null,
                0,
                "케이크",
                "케이크 구성",
                0,
                List.of(new OrderFormFieldOptionCommand("1호", "size-1", true, 0))),
            new CreateOrderFormCommand.Field(
                "요청사항",
                FieldType.TEXTAREA,
                false,
                "{\"maxLength\":100}",
                0,
                "추가 요청",
                null,
                1,
                List.of()))));

    OrderFormResult updated = orderFormService.update(new UpdateOrderFormCommand(
        store.id(),
        created.id(),
        "수정 주문서",
        List.of(new UpdateOrderFormCommand.Field(
            "요청사항",
            FieldType.TEXTAREA,
            false,
            "{\"placeholder\":\"입력\"}",
            0,
            "추가 요청",
            null,
            0,
            List.of()))));

    assertEquals(2, created.groups().size());
    assertEquals("케이크", created.groups().get(0).title());
    assertEquals(
        "size-1", created.groups().get(0).fields().get(0).options().get(0).value());
    assertEquals(1, updated.groups().size());
    assertEquals("추가 요청", updated.groups().get(0).title());
  }

  @Test
  @DisplayName("필드 유형과 맞지 않는 settings 및 선택지 정의를 거부한다")
  void rejectsInvalidTypeSpecificSettings() {
    StoreResult store = createStore();

    BaseException imageException = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.Field(
                "참고 이미지", FieldType.IMAGE, false, "{\"maxCount\":6}", 0)))));
    BaseException optionException = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(
                new CreateOrderFormCommand.Field("크기", FieldType.SINGLE_SELECT, true, null, 0)))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, imageException.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, optionException.getErrorCode());
  }

  private StoreResult createStore() {
    User seller = userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(), uniqueEmail("order-form-seller"), "판매자", UserRole.SELLER));
    return storeService.create(new CreateStoreCommand(
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
  }

  private CreateOrderFormCommand createCommand(UUID storeId, String name) {
    return new CreateOrderFormCommand(
        storeId,
        name,
        List.of(new CreateOrderFormCommand.Field("메뉴명", FieldType.TEXT, true, null, 0)));
  }
}
