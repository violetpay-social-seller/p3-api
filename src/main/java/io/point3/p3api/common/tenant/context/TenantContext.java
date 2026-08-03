package io.point3.p3api.common.tenant.context;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TenantContext {

    private static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    public static ScopedValue.Carrier where(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new BaseException(CommonErrorCode.INVALID_INPUT);
        }

        return ScopedValue.where(TENANT_ID, tenantId);
    }

    public static String getTenantId() {
        return TENANT_ID.isBound() ? TENANT_ID.get() : null;
    }

    public static String getRequiredTenantId() {
        String tenantId = getTenantId();

        if (tenantId == null || tenantId.isBlank()) {
            throw new BaseException(CommonErrorCode.INVALID_INPUT);
        }

        return tenantId;
    }

    public static boolean hasTenant() {
        return TENANT_ID.isBound() && !TENANT_ID.get().isBlank();
    }
}
