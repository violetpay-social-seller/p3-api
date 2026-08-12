package io.point3.p3api.common.tenant;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 현재 로그인한 판매자의 스토어를 찾는 컴포넌트
 * 상태 저장x 단순 조회결과 반환 서비스
 */
@Component
@RequiredArgsConstructor
public class SellerStoreProvider {

    private final StorePersistencePort storePersistencePort;

    @Transactional(readOnly = true)
    public Store resolveStore(CurrentUser currentUser) {
        return storePersistencePort.findByOwnerUserId(currentUser.userId())
                .orElseThrow(() -> new IllegalArgumentException());// TODO : 예외 교체 필요
    }

    @Transactional(readOnly = true)
    public UUID resolveStoreId(CurrentUser currentUser) {
        return resolveStore(currentUser).getId();
    }
}
