package io.point3.p3api.common.tenant.access;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;

@FunctionalInterface
public interface TenantAccessChecker {

  void check(CurrentUser currentUser, String tenantId);
}
