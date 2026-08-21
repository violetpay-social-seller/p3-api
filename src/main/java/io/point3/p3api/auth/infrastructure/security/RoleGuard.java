package io.point3.p3api.auth.infrastructure.security;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.domain.type.UserRole;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RoleGuard {

  public static void requireSeller(CurrentUser currentUser) {
    requiredRole(currentUser, UserRole.SELLER);
  }

  public static void requireBuyer(CurrentUser currentUser) {
    requiredRole(currentUser, UserRole.BUYER);
  }

  public static void requireOperator(CurrentUser currentUser) {
    requiredRole(currentUser, UserRole.OPERATOR);
  }

  private static void requiredRole(CurrentUser currentUser, UserRole role) {
    if (currentUser == null || currentUser.role() != role) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED);
    }
  }
}
