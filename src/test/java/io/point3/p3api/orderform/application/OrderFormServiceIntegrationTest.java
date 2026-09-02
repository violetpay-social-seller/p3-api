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
import io.point3.p3api.orderform.domain.type.OptionInputType;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import io.point3.p3api.orderform.domain.type.SelectionType;
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
  @DisplayName("옵션그룹 sortOrder가 0부터 연속되지 않으면 주문서 양식을 생성할 수 없다")
  void rejectsNonSequentialOptionGroupSortOrder() {
    StoreResult store = createStore();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(createGroup(
                "메뉴명",
                SelectionType.SINGLE,
                true,
                1,
                OrderFormCategory.SIZE,
                0,
                option("메뉴명", "menu", OptionInputType.TEXT, 0L, null, 0))))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("옵션 settings는 JSON object만 허용한다")
  void rejectsInvalidOptionSettings() {
    StoreResult store = createStore();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(createGroup(
                "메뉴명",
                SelectionType.SINGLE,
                true,
                0,
                OrderFormCategory.SIZE,
                0,
                option("메뉴명", "menu", OptionInputType.TEXT, 0L, "\"not-object\"", 0))))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("주문서 수정은 기존 옵션그룹을 교체하고 비활성화하면 활성 양식 조회에서 제외한다")
  void updatesAndInactivatesTemplate() {
    StoreResult store = createStore();
    OrderFormResult created = orderFormService.create(createCommand(store.id(), "기본 주문서"));

    OrderFormResult updated = orderFormService.update(new UpdateOrderFormCommand(
        store.id(),
        created.id(),
        "수정 주문서",
        List.of(
            updateGroup(
                "메뉴명",
                SelectionType.SINGLE,
                true,
                0,
                OrderFormCategory.SIZE,
                0,
                option("메뉴명", "menu", OptionInputType.TEXT, 0L, null, 0)),
            updateGroup(
                "요청사항",
                SelectionType.SINGLE,
                false,
                1,
                OrderFormCategory.SIZE,
                0,
                option("요청사항", "memo", OptionInputType.TEXTAREA, null, null, 0)))));
    OrderFormResult inactive = orderFormService.inactive(store.id(), created.id());
    BaseException exception =
        assertThrows(BaseException.class, () -> orderFormService.getActiveTemplate(store.id()));

    assertEquals("수정 주문서", updated.name());
    assertEquals(2, updated.optionGroups().size());
    assertFalse(inactive.active());
    assertEquals(OrderFormErrorCode.ORDER_FORM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("카테고리/옵션그룹/옵션 정의를 순서대로 저장하고 수정할 수 있다")
  void savesCategoryGroupsOptionGroupsAndOptions() {
    StoreResult store = createStore();
    OrderFormResult created = orderFormService.create(new CreateOrderFormCommand(
        store.id(),
        "주문서",
        List.of(
            createGroup(
                "케이크 크기",
                SelectionType.SINGLE,
                true,
                0,
                OrderFormCategory.SIZE,
                0,
                option("1호", "size-1", OptionInputType.SELECT, 10000L, null, 0)),
            createGroup(
                "요청사항",
                SelectionType.SINGLE,
                false,
                0,
                OrderFormCategory.OTHER_REQUEST,
                5,
                option(
                    "요청사항", "memo", OptionInputType.TEXTAREA, null, "{\"maxLength\":100}", 0)))));

    OrderFormResult updated = orderFormService.update(new UpdateOrderFormCommand(
        store.id(),
        created.id(),
        "수정 주문서",
        List.of(updateGroup(
            "요청사항",
            SelectionType.SINGLE,
            false,
            0,
            OrderFormCategory.OTHER_REQUEST,
            5,
            option(
                "요청사항", "memo", OptionInputType.TEXTAREA, null, "{\"placeholder\":\"입력\"}", 0)))));

    assertEquals(2, created.groups().size());
    assertEquals(OrderFormCategory.SIZE, created.groups().get(0).category());
    assertEquals("사이즈", created.groups().get(0).title());
    assertEquals(
        "size-1", created.groups().get(0).optionGroups().get(0).options().get(0).value());
    assertEquals(
        10000, created.groups().get(0).optionGroups().get(0).options().get(0).price());
    assertEquals(1, updated.groups().size());
    assertEquals(OrderFormCategory.OTHER_REQUEST, updated.groups().get(0).category());
    assertEquals("기타 요청사항", updated.groups().get(0).title());
  }

  @Test
  @DisplayName("복수 선택 옵션그룹은 케이크 디자인 카테고리에서만 허용한다")
  void allowsMultiSelectOnlyForCakeDesignCategory() {
    StoreResult store = createStore();

    CreateOrderFormCommand.OptionGroup cakeDesignMultiSelect = createGroup(
        "디자인 옵션",
        SelectionType.MULTI,
        true,
        0,
        OrderFormCategory.CAKE_DESIGN,
        3,
        option("플라워", "flower", OptionInputType.SELECT, 5000L, null, 0),
        option("리본", "ribbon", OptionInputType.SELECT, 2000L, null, 1));
    CreateOrderFormCommand.OptionGroup flavorMultiSelect = createGroup(
        "맛 옵션",
        SelectionType.MULTI,
        true,
        0,
        OrderFormCategory.CAKE_FLAVOR,
        2,
        option("초코", "choco", OptionInputType.SELECT, 0L, null, 0),
        option("딸기", "strawberry", OptionInputType.SELECT, 0L, null, 1));

    OrderFormResult created = orderFormService.create(
        new CreateOrderFormCommand(store.id(), "주문서", List.of(cakeDesignMultiSelect)));

    StoreResult flavorStore = createStore();
    BaseException exception = assertThrows(
        BaseException.class,
        () -> orderFormService.create(
            new CreateOrderFormCommand(flavorStore.id(), "잘못된 주문서", List.of(flavorMultiSelect))));

    assertEquals(
        SelectionType.MULTI, created.groups().get(0).optionGroups().get(0).selectionType());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("옵션 입력 유형과 맞지 않는 settings 및 빈 옵션 정의를 거부한다")
  void rejectsInvalidTypeSpecificSettings() {
    StoreResult store = createStore();

    BaseException imageException = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(createGroup(
                "참고 이미지",
                SelectionType.SINGLE,
                false,
                0,
                OrderFormCategory.SIZE,
                0,
                option(
                    "참고 이미지", "reference", OptionInputType.IMAGE, null, "{\"maxCount\":6}", 0))))));
    BaseException optionException = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.OptionGroup(
                "크기",
                SelectionType.SINGLE,
                true,
                0,
                OrderFormCategory.SIZE,
                OrderFormCategory.SIZE.getTitle(),
                null,
                0,
                List.of())))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, imageException.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, optionException.getErrorCode());
  }

  @Test
  @DisplayName("옵션 유형별 설명 settings를 저장하고 조회 결과에 유지한다")
  void preservesTypeSpecificDescriptionSettings() {
    StoreResult store = createStore();

    OrderFormResult created = orderFormService.create(new CreateOrderFormCommand(
        store.id(),
        "주문서",
        List.of(createGroup(
            "추가 요청",
            SelectionType.SINGLE,
            false,
            0,
            OrderFormCategory.SIZE,
            0,
            option(
                "레터링",
                "lettering",
                OptionInputType.SELECT_WITH_TEXT,
                3000L,
                "{\"placeholder\":\"레터링 내용을 입력하세요\",\"helperText\":\"필기체 가능\",\"unknown\":true}",
                0),
            option(
                "참고 이미지",
                "reference",
                OptionInputType.IMAGE,
                0L,
                "{\"maxCount\":3,\"allowedContentTypes\":[\"image/jpeg\"],\"helperText\":\"이미지를 첨부하세요\",\"unknown\":true}",
                1),
            option(
                "요청사항",
                "memo",
                OptionInputType.TEXTAREA,
                null,
                "{\"placeholder\":\"요청사항을 입력하세요\",\"helperText\":\"미작성 시 반영되지 않음\",\"maxLength\":500,\"unknown\":true}",
                2)))));

    List<String> settings = created.groups().get(0).optionGroups().get(0).options().stream()
        .map(option -> option.settings())
        .toList();

    assertEquals("{\"placeholder\":\"레터링 내용을 입력하세요\",\"helperText\":\"필기체 가능\"}", settings.get(0));
    assertEquals(
        "{\"maxCount\":3,\"allowedContentTypes\":[\"image/jpeg\"],\"helperText\":\"이미지를 첨부하세요\"}",
        settings.get(1));
    assertEquals(
        "{\"placeholder\":\"요청사항을 입력하세요\",\"helperText\":\"미작성 시 반영되지 않음\",\"maxLength\":500}",
        settings.get(2));
  }

  @Test
  @DisplayName("문자열이 아닌 helperText는 주문서 옵션 settings로 허용하지 않는다")
  void rejectsNonTextHelperText() {
    BaseException numberException =
        assertThrows(BaseException.class, () -> createWithHelperText("1"));
    BaseException arrayException =
        assertThrows(BaseException.class, () -> createWithHelperText("[]"));
    BaseException objectException =
        assertThrows(BaseException.class, () -> createWithHelperText("{}"));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, numberException.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, arrayException.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, objectException.getErrorCode());
  }

  @Test
  @DisplayName("고정 카테고리와 옵션 가격 정책을 벗어나면 주문서 정의를 거부한다")
  void rejectsInvalidCategoryAndPricePolicy() {
    StoreResult store = createStore();

    BaseException categoryException = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(new CreateOrderFormCommand.OptionGroup(
                "메뉴명",
                SelectionType.SINGLE,
                true,
                0,
                OrderFormCategory.SIZE,
                "기본 정보",
                null,
                0,
                List.of(option("메뉴명", "menu", OptionInputType.TEXT, 0L, null, 0)))))));
    BaseException missingTextPrice = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(createGroup(
                "메뉴명",
                SelectionType.SINGLE,
                true,
                0,
                OrderFormCategory.SIZE,
                0,
                option("메뉴명", "menu", OptionInputType.TEXT, null, null, 0))))));
    BaseException textareaPrice = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(createGroup(
                "요청사항",
                SelectionType.SINGLE,
                false,
                0,
                OrderFormCategory.SIZE,
                0,
                option("요청사항", "memo", OptionInputType.TEXTAREA, 1000L, null, 0))))));
    BaseException tooManyOptions = assertThrows(
        BaseException.class,
        () -> orderFormService.create(new CreateOrderFormCommand(
            store.id(),
            "주문서",
            List.of(createGroup(
                "크기",
                SelectionType.SINGLE,
                true,
                0,
                OrderFormCategory.SIZE,
                0,
                option("1호", "size-1", OptionInputType.SELECT, 0L, null, 0),
                option("2호", "size-2", OptionInputType.SELECT, 0L, null, 1),
                option("3호", "size-3", OptionInputType.SELECT, 0L, null, 2),
                option("4호", "size-4", OptionInputType.SELECT, 0L, null, 3),
                option("5호", "size-5", OptionInputType.SELECT, 0L, null, 4),
                option("6호", "size-6", OptionInputType.SELECT, 0L, null, 5),
                option("7호", "size-7", OptionInputType.SELECT, 0L, null, 6))))));

    assertEquals(
        OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, categoryException.getErrorCode());
    assertEquals(
        OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, missingTextPrice.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, textareaPrice.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, tooManyOptions.getErrorCode());
  }

  @Test
  @DisplayName("가격 문구를 숫자 가격과 구분해 저장하고 수정할 수 있다")
  void preservesPriceLabelOnCreateAndUpdate() {
    StoreResult store = createStore();
    OrderFormResult created = orderFormService.create(new CreateOrderFormCommand(
        store.id(),
        "주문서",
        List.of(createGroup(
            "케이크 크기",
            SelectionType.SINGLE,
            true,
            0,
            OrderFormCategory.SIZE,
            0,
            optionWithPriceLabel("맞춤 크기", "custom-size", OptionInputType.SELECT, " 문의필요 ", 0)))));

    OrderFormResult updated = orderFormService.update(new UpdateOrderFormCommand(
        store.id(),
        created.id(),
        "수정 주문서",
        List.of(updateGroup(
            "참고 이미지",
            SelectionType.SINGLE,
            false,
            0,
            OrderFormCategory.SIZE,
            0,
            optionWithPriceLabel("맞춤 이미지", "custom-image", OptionInputType.IMAGE, "가격 협의", 0)))));

    assertEquals(
        "문의필요", created.groups().get(0).optionGroups().get(0).options().get(0).priceLabel());
    assertEquals(
        null, created.groups().get(0).optionGroups().get(0).options().get(0).price());
    assertEquals(
        "가격 협의", updated.groups().get(0).optionGroups().get(0).options().get(0).priceLabel());
    assertEquals(
        null, updated.groups().get(0).optionGroups().get(0).options().get(0).price());
  }

  @Test
  @DisplayName("옵션 가격 문구 정책을 벗어나면 주문서 정의를 거부한다")
  void rejectsInvalidPriceLabelPolicy() {
    StoreResult store = createStore();

    BaseException bothPriceAndLabel = assertThrows(
        BaseException.class,
        () -> createWithOption(
            store.id(),
            optionWithPriceAndLabel(
                "맞춤 크기", "custom-size", OptionInputType.SELECT, 1000L, "문의필요", 0)));
    BaseException missingPriceAndLabel = assertThrows(
        BaseException.class,
        () -> createWithOption(
            store.id(), option("맞춤 크기", "custom-size", OptionInputType.SELECT, null, null, 0)));
    BaseException blankLabel = assertThrows(
        BaseException.class,
        () -> createWithOption(
            store.id(),
            optionWithPriceLabel("맞춤 크기", "custom-size", OptionInputType.SELECT, "   ", 0)));
    BaseException tooLongLabel = assertThrows(
        BaseException.class,
        () -> createWithOption(
            store.id(),
            optionWithPriceLabel(
                "맞춤 크기", "custom-size", OptionInputType.SELECT, "a".repeat(101), 0)));
    BaseException textareaLabel = assertThrows(
        BaseException.class,
        () -> createWithOption(
            store.id(), optionWithPriceLabel("요청사항", "memo", OptionInputType.TEXTAREA, "문의필요", 0)));

    assertEquals(
        OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, bothPriceAndLabel.getErrorCode());
    assertEquals(
        OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, missingPriceAndLabel.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, blankLabel.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, tooLongLabel.getErrorCode());
    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, textareaLabel.getErrorCode());
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
        List.of(createGroup(
            "메뉴명",
            SelectionType.SINGLE,
            true,
            0,
            OrderFormCategory.SIZE,
            0,
            option("메뉴명", "menu", OptionInputType.TEXT, 0L, null, 0))));
  }

  private void createWithHelperText(String helperText) {
    StoreResult store = createStore();
    orderFormService.create(new CreateOrderFormCommand(
        store.id(),
        "주문서",
        List.of(createGroup(
            "요청사항",
            SelectionType.SINGLE,
            false,
            0,
            OrderFormCategory.SIZE,
            0,
            option(
                "요청사항",
                "memo",
                OptionInputType.TEXTAREA,
                null,
                "{\"helperText\":" + helperText + "}",
                0)))));
  }

  private void createWithOption(UUID storeId, OrderFormOptionCommand option) {
    orderFormService.create(new CreateOrderFormCommand(
        storeId,
        "주문서",
        List.of(
            createGroup("옵션", SelectionType.SINGLE, true, 0, OrderFormCategory.SIZE, 0, option))));
  }

  private CreateOrderFormCommand.OptionGroup createGroup(
      String label,
      SelectionType selectionType,
      boolean required,
      int sortOrder,
      OrderFormCategory category,
      int groupSortOrder,
      OrderFormOptionCommand... options) {
    return new CreateOrderFormCommand.OptionGroup(
        label,
        selectionType,
        required,
        sortOrder,
        category,
        category.getTitle(),
        null,
        groupSortOrder,
        List.of(options));
  }

  private UpdateOrderFormCommand.OptionGroup updateGroup(
      String label,
      SelectionType selectionType,
      boolean required,
      int sortOrder,
      OrderFormCategory category,
      int groupSortOrder,
      OrderFormOptionCommand... options) {
    return new UpdateOrderFormCommand.OptionGroup(
        label,
        selectionType,
        required,
        sortOrder,
        category,
        category.getTitle(),
        null,
        groupSortOrder,
        List.of(options));
  }

  private OrderFormOptionCommand option(
      String label,
      String value,
      OptionInputType inputType,
      Long price,
      String settings,
      int sortOrder) {
    return new OrderFormOptionCommand(
        label, value, inputType, price, null, settings, true, sortOrder);
  }

  private OrderFormOptionCommand optionWithPriceLabel(
      String label, String value, OptionInputType inputType, String priceLabel, int sortOrder) {
    return new OrderFormOptionCommand(
        label, value, inputType, null, priceLabel, null, true, sortOrder);
  }

  private OrderFormOptionCommand optionWithPriceAndLabel(
      String label,
      String value,
      OptionInputType inputType,
      Long price,
      String priceLabel,
      int sortOrder) {
    return new OrderFormOptionCommand(
        label, value, inputType, price, priceLabel, null, true, sortOrder);
  }
}
