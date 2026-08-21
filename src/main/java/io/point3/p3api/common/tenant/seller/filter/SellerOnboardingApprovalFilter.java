package io.point3.p3api.common.tenant.seller.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.seller.provider.SellerOnboardingApprovalProvider;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.common.web.response.ErrorResult;
import io.point3.p3api.exception.BaseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class SellerOnboardingApprovalFilter extends OncePerRequestFilter {

  private static final String SELLER_PREFIX = "/seller/";
  private static final String SELLER_ONBOARDINGS_PATH = "/seller/onboardings";

  private final CurrentUserRender currentUserRender;
  private final SellerOnboardingApprovalProvider sellerOnboardingApprovalProvider;
  private final ObjectMapper objectMapper;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return !path.startsWith(SELLER_PREFIX)
        || path.equals(SELLER_ONBOARDINGS_PATH)
        || path.startsWith(SELLER_ONBOARDINGS_PATH + "/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      CurrentUser currentUser = currentUserRender.read(authentication);
      RoleGuard.requireSeller(currentUser);
      sellerOnboardingApprovalProvider.requireApproved(currentUser);
    } catch (BaseException e) {
      writeError(response, request, e);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void writeError(HttpServletResponse response, HttpServletRequest request, BaseException e)
      throws IOException {
    response.setStatus(e.getErrorCode().getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(
        response.getOutputStream(),
        ApiResponse.fail(ErrorResult.of(e.getErrorCode(), request.getRequestURI())));
  }
}
