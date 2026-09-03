package io.point3.p3api.store.application.management;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.domain.entity.StoreNotice;
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

class StoreManagementStatusQueryServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private StoreManagementStatusQueryService storeManagementStatusQueryService;

  @Autowired
  private StoreNoticePersistencePort storeNoticePersistencePort;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("5개 공지가 모두 비공백일 때만 스토어 관리 공지 항목을 완료로 판단한다")
  void completesNoticeOnlyWhenAllNoticesHaveContent() {
    StoreResult store = createStore();
    storeNoticePersistencePort.replaceAllByStoreId(
        store.id(),
        List.of(
            notice(store.id(), StoreNoticeType.PICKUP_DELIVERY, "픽업 안내"),
            notice(store.id(), StoreNoticeType.DESIGN_PRODUCTION, "디자인 안내"),
            notice(store.id(), StoreNoticeType.PAYMENT, "결제 안내"),
            notice(store.id(), StoreNoticeType.CAKE_CARE, "보관 안내"),
            notice(store.id(), StoreNoticeType.BUSINESS_HOURS, " ")));

    boolean blankNotice =
        storeManagementStatusQueryService.getStatus(store.id()).items().notice();

    storeNoticePersistencePort.replaceAllByStoreId(
        store.id(),
        List.of(
            notice(store.id(), StoreNoticeType.PICKUP_DELIVERY, "픽업 안내"),
            notice(store.id(), StoreNoticeType.DESIGN_PRODUCTION, "디자인 안내"),
            notice(store.id(), StoreNoticeType.PAYMENT, "결제 안내"),
            notice(store.id(), StoreNoticeType.CAKE_CARE, "보관 안내"),
            notice(store.id(), StoreNoticeType.BUSINESS_HOURS, "영업시간 안내")));

    boolean completedNotice =
        storeManagementStatusQueryService.getStatus(store.id()).items().notice();

    assertFalse(blankNotice);
    assertTrue(completedNotice);
  }

  private StoreNotice notice(UUID storeId, StoreNoticeType type, String content) {
    return StoreNotice.create(storeId, type, content);
  }

  private StoreResult createStore() {
    User seller = userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("store-management-seller"),
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
