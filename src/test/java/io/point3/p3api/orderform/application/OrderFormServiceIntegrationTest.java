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
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
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
            List.of(new CreateOrderFormCommand.Field(
                "메뉴명",
                FieldType.TEXT,
                true,
                0L,
                null,
                1,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of())))));

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
                "메뉴명",
                FieldType.TEXT,
                true,
                0L,
                "\"not-object\"",
                0,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of())))));

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
            new UpdateOrderFormCommand.Field(
                "메뉴명",
                FieldType.TEXT,
                true,
                0L,
                null,
                0,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of()),
            new UpdateOrderFormCommand.Field(
                "요청사항",
                FieldType.TEXTAREA,
                false,
                null,
                null,
                1,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of()))));
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
                null,
                0,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of(new OrderFormFieldOptionCommand("1호", "size-1", 10000L, true, 0))),
            new CreateOrderFormCommand.Field(
                "요청사항",
                FieldType.TEXTAREA,
                false,
                null,
                "{\"maxLength\":100}",
                0,
                OrderFormCategory.OTHER_REQUEST,
                OrderFormCategory.OTHER_REQUEST.getTitle(),
                null,
                5,
                List.of()))));

    OrderFormResult updated = orderFormService.update(new UpdateOrderFormCommand(
        store.id(),
        created.id(),
        "수정 주문서",
        List.of(new UpdateOrderFormCommand.Field(
            "요청사항",
            FieldType.TEXTAREA,
            false,
            null,
            "{\"placeholder\":\"입력\"}",
            0,
            OrderFormCategory.OTHER_REQUEST,
            OrderFormCategory.OTHER_REQUEST.getTitle(),
            null,
            5,
            List.of()))));

    assertEquals(2, created.groups().size());
    assertEquals(OrderFormCategory.DESIGN, created.groups().get(0).category());
    assertEquals("디자인", created.groups().get(0).title());
    assertEquals(
        "size-1", created.groups().get(0).fields().get(0).options().get(0).value());
    assertEquals(10000, created.groups().get(0).fields().get(0).options().get(0).price());
    assertEquals(1, updated.groups().size());
    assertEquals(OrderFormCategory.OTHER_REQUEST, updated.groups().get(0).category());
    assertEquals("기타 요청사항", updated.groups().get(0).title());
  }

  @Test
  @DisplayName("복수 선택 필드는 케이크 디자인 카테고리에서만 허용한다")
  void allowsMultiSelectOnlyForCakeDesignCategory() {
    StoreResult store = createStore();

    CreateOrderFormCommand.Field cakeDesignMultiSelect = new CreateOrderFormCommand.Field(
        "디자인 옵션",
        FieldType.MULTI_SELECT,
        true,
        null,
        null,
        0,
        OrderFormCategory.CAKE_DESIGN,
        OrderFormCategory.CAKE_DESIGN.getTitle(),
        null,
        3,
        List.of(
            new OrderFormFieldOptionCommand("플라워", "flower", 5000L, true, 0),
            new OrderFormFieldOptionCommand("리본", "ribbon", 2000L, true, 1)));
    CreateOrderFormCommand.Field flavorMultiSelect = new CreateOrderFormCommand.Field(
        "맛 옵션",
        FieldType.MULTI_SELECT,
        true,
        null,
        null,
        0,
        OrderFormCategory.CAKE_FLAVOR,
        OrderFormCategory.CAKE_FLAVOR.getTitle(),
        null,
        2,
        List.of(
            new OrderFormFieldOptionCommand("초코", "choco", 0L, true, 0),
            new OrderFormFieldOptionCommand("딸기", "strawberry", 0L, true, 1)));

    OrderFormResult created = orderFormService.create(
        new CreateOrderFormCommand(store.id(), "주문서", List.of(cakeDesignMultiSelect)));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderFormService.create(
            new CreateOrderFormCommand(store.id(), "잘못된 주문서", List.of(flavorMultiSelect))));

    assertEquals(FieldType.MULTI_SELECT, created.groups().get(0).fields().get(0).fieldType());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
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
                "참고 이미지",
                FieldType.IMAGE,
                false,
                null,
                "{\"maxCount\":6}",
                0,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of())))));
    BaseException optionException = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.Field(
                "크기",
                FieldType.SINGLE_SELECT,
                true,
                null,
                null,
                0,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of())))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, imageException.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, optionException.getErrorCode());
  }

  @Test
  @DisplayName("고정 카테고리와 필드/옵션 가격 정책을 벗어나면 주문서 정의를 거부한다")
  void rejectsInvalidCategoryAndPricePolicy() {
    StoreResult store = createStore();

    BaseException categoryException = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.Field(
                "메뉴명",
                FieldType.TEXT,
                true,
                0L,
                null,
                0,
                OrderFormCategory.DESIGN,
                "기본 정보",
                null,
                0,
                List.of())))));
    BaseException missingTextPrice = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.Field(
                "메뉴명",
                FieldType.TEXT,
                true,
                null,
                null,
                0,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of())))));
    BaseException textareaPrice = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.Field(
                "요청사항",
                FieldType.TEXTAREA,
                false,
                1000L,
                null,
                0,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of())))));
    BaseException tooManyOptions = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.Field(
                "크기",
                FieldType.SINGLE_SELECT,
                true,
                null,
                null,
                0,
                OrderFormCategory.DESIGN,
                OrderFormCategory.DESIGN.getTitle(),
                null,
                0,
                List.of(
                    new OrderFormFieldOptionCommand("1호", "size-1", 0L, true, 0),
                    new OrderFormFieldOptionCommand("2호", "size-2", 0L, true, 1),
                    new OrderFormFieldOptionCommand("3호", "size-3", 0L, true, 2),
                    new OrderFormFieldOptionCommand("4호", "size-4", 0L, true, 3),
                    new OrderFormFieldOptionCommand("5호", "size-5", 0L, true, 4),
                    new OrderFormFieldOptionCommand("6호", "size-6", 0L, true, 5)))))));

    assertEquals(
        OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, categoryException.getErrorCode());
    assertEquals(
        OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, missingTextPrice.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, textareaPrice.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, tooManyOptions.getErrorCode());
  }

  private StoreResult createStore() {
    User seller = userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("order-form-seller"),
        "판매자",
        UserRole.SELLER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
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
        List.of(new CreateOrderFormCommand.Field(
            "메뉴명",
            FieldType.TEXT,
            true,
            0L,
            null,
            0,
            OrderFormCategory.DESIGN,
            OrderFormCategory.DESIGN.getTitle(),
            null,
            0,
            List.of())));
  }
}
