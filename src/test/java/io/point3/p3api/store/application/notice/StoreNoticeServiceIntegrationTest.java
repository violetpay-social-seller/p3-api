package io.point3.p3api.store.application.notice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.notice.command.UpdateStoreNoticesCommand;
import io.point3.p3api.store.application.notice.result.StoreNoticeResult;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.domain.type.StoreNoticeType;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StoreNoticeServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private StoreNoticeService storeNoticeService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("타입별 여러 공지를 순서대로 저장하고 빈 탭은 빈 배열로 조회한다")
  void savesMultipleNoticeItemsInFixedOrder() {
    StoreResult store = createStore();

    StoreNoticeResult updated = storeNoticeService.update(new UpdateStoreNoticesCommand(
        store.id(),
        notices(
            List.of("픽업 안내", "예약 변경 안내"),
            List.of(),
            List.of("결제 안내"),
            List.of("보관 안내"),
            List.of("영업시간 안내"))));
    StoreNoticeResult found = storeNoticeService.getNotices(store.id());

    assertEquals(
        List.of(StoreNoticeType.values()),
        updated.notices().stream().map(StoreNoticeResult.Notice::type).toList());
    assertEquals(2, updated.notices().getFirst().items().size());
    assertEquals("예약 변경 안내", updated.notices().getFirst().items().get(1).content());
    assertEquals(1, updated.notices().getFirst().items().get(1).sortOrder());
    assertEquals(List.of(), found.notices().get(1).items());
  }

  @Test
  @DisplayName("5개 타입을 모두 포함하지 않거나 중복 타입이면 전체 저장을 거절한다")
  void rejectsMissingOrDuplicateNoticeType() {
    StoreResult store = createStore();

    BaseException missingException = assertThrows(
        BaseException.class,
        () -> storeNoticeService.update(new UpdateStoreNoticesCommand(
            store.id(),
            notices(List.of("픽업 안내"), List.of("디자인 안내"), List.of(), List.of(), List.of())
                .subList(0, 4))));
    BaseException duplicateException = assertThrows(
        BaseException.class,
        () -> storeNoticeService.update(new UpdateStoreNoticesCommand(
            store.id(),
            List.of(
                notice(StoreNoticeType.PICKUP_DELIVERY, "픽업 안내"),
                notice(StoreNoticeType.PICKUP_DELIVERY, "중복 안내"),
                notice(StoreNoticeType.PAYMENT, "결제 안내"),
                notice(StoreNoticeType.CAKE_CARE, "보관 안내"),
                notice(StoreNoticeType.BUSINESS_HOURS, "영업시간 안내")))));

    assertEquals(CommonErrorCode.INVALID_INPUT, missingException.getErrorCode());
    assertEquals(CommonErrorCode.INVALID_INPUT, duplicateException.getErrorCode());
  }

  @Test
  @DisplayName("공백 문구는 전체 저장을 거절한다")
  void rejectsBlankNoticeItem() {
    StoreResult store = createStore();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> storeNoticeService.update(new UpdateStoreNoticesCommand(
            store.id(), notices(List.of(" "), List.of(), List.of(), List.of(), List.of()))));

    assertEquals(CommonErrorCode.INVALID_INPUT, exception.getErrorCode());
  }

  private List<UpdateStoreNoticesCommand.Notice> notices(
      List<String> pickupDelivery,
      List<String> designProduction,
      List<String> payment,
      List<String> cakeCare,
      List<String> businessHours) {
    return List.of(
        notice(StoreNoticeType.PICKUP_DELIVERY, pickupDelivery),
        notice(StoreNoticeType.DESIGN_PRODUCTION, designProduction),
        notice(StoreNoticeType.PAYMENT, payment),
        notice(StoreNoticeType.CAKE_CARE, cakeCare),
        notice(StoreNoticeType.BUSINESS_HOURS, businessHours));
  }

  private UpdateStoreNoticesCommand.Notice notice(StoreNoticeType type, String content) {
    return notice(type, List.of(content));
  }

  private UpdateStoreNoticesCommand.Notice notice(StoreNoticeType type, List<String> contents) {
    return new UpdateStoreNoticesCommand.Notice(
        type, contents.stream().map(UpdateStoreNoticesCommand.Item::new).toList());
  }

  private StoreResult createStore() {
    User seller = userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("store-notice-seller"),
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
}
