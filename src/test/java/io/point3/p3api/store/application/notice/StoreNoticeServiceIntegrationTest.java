package io.point3.p3api.store.application.notice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
  @DisplayName("스토어 공지는 고정 타입 순서로 조회하고 null 콘텐츠는 미작성으로 반환한다")
  void savesAndGetsNoticesInFixedOrder() {
    StoreResult store = createStore();

    storeNoticeService.update(new UpdateStoreNoticesCommand(
        store.id(), notices("픽업 안내", "디자인 안내", "결제 안내", "보관 안내", "영업시간 안내")));
    StoreNoticeResult updated = storeNoticeService.update(new UpdateStoreNoticesCommand(
        store.id(), notices("픽업 안내", null, "결제 안내", "보관 안내", "영업시간 안내")));
    StoreNoticeResult found = storeNoticeService.getNotices(store.id());

    assertEquals(
        List.of(StoreNoticeType.values()),
        updated.notices().stream().map(StoreNoticeResult.Notice::type).toList());
    assertNull(updated.notices().get(1).content());
    assertEquals("결제 안내", found.notices().get(2).content());
  }

  @Test
  @DisplayName("5개 공지 타입을 모두 포함하지 않으면 전체 저장을 거절한다")
  void rejectsMissingNoticeType() {
    StoreResult store = createStore();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> storeNoticeService.update(new UpdateStoreNoticesCommand(
            store.id(),
            List.of(
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.PICKUP_DELIVERY, "픽업 안내"),
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.DESIGN_PRODUCTION, "디자인 안내"),
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.PAYMENT, "결제 안내"),
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.CAKE_CARE, "보관 안내")))));

    assertEquals(CommonErrorCode.INVALID_INPUT, exception.getErrorCode());
  }

  @Test
  @DisplayName("중복 공지 타입 또는 공백 콘텐츠를 포함하면 전체 저장을 거절한다")
  void rejectsDuplicateTypeAndBlankContent() {
    StoreResult store = createStore();

    BaseException duplicateException = assertThrows(
        BaseException.class,
        () -> storeNoticeService.update(new UpdateStoreNoticesCommand(
            store.id(),
            List.of(
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.PICKUP_DELIVERY, "픽업 안내"),
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.PICKUP_DELIVERY, "중복 안내"),
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.PAYMENT, "결제 안내"),
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.CAKE_CARE, "보관 안내"),
                new UpdateStoreNoticesCommand.Notice(StoreNoticeType.BUSINESS_HOURS, "영업시간 안내")))));
    BaseException blankException = assertThrows(
        BaseException.class,
        () -> storeNoticeService.update(new UpdateStoreNoticesCommand(
            store.id(), notices("픽업 안내", "디자인 안내", " ", "보관 안내", "영업시간 안내"))));

    assertEquals(CommonErrorCode.INVALID_INPUT, duplicateException.getErrorCode());
    assertEquals(CommonErrorCode.INVALID_INPUT, blankException.getErrorCode());
  }

  private List<UpdateStoreNoticesCommand.Notice> notices(String... contents) {
    return List.of(
        new UpdateStoreNoticesCommand.Notice(StoreNoticeType.PICKUP_DELIVERY, contents[0]),
        new UpdateStoreNoticesCommand.Notice(StoreNoticeType.DESIGN_PRODUCTION, contents[1]),
        new UpdateStoreNoticesCommand.Notice(StoreNoticeType.PAYMENT, contents[2]),
        new UpdateStoreNoticesCommand.Notice(StoreNoticeType.CAKE_CARE, contents[3]),
        new UpdateStoreNoticesCommand.Notice(StoreNoticeType.BUSINESS_HOURS, contents[4]));
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
