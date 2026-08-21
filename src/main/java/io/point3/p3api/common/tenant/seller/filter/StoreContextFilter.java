package io.point3.p3api.common.tenant.seller.filter;

import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.context.StoreContext;
import io.point3.p3api.common.tenant.seller.provider.SellerStoreProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class StoreContextFilter extends OncePerRequestFilter {

  private static final String SELLER_PREFIX = "/seller/";
  private static final String SELLER_STORE_PATH = "/seller/store";
  private static final String SELLER_ONBOARDINGS_PATH = "/seller/onboardings";

  private final CurrentUserRender currentUserRender;
  private final SellerStoreProvider sellerStoreProvider;

  public StoreContextFilter(
      CurrentUserRender currentUserRender, SellerStoreProvider sellerStoreProvider) {
    this.currentUserRender = currentUserRender;
    this.sellerStoreProvider = sellerStoreProvider;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (!path.startsWith(SELLER_PREFIX)) {
      return true;
    }

    if (path.equals(SELLER_ONBOARDINGS_PATH)
        || path.startsWith(SELLER_ONBOARDINGS_PATH + "/")) {
      return true;
    }

    if (!"POST".equals(request.getMethod())) {
      return false;
    }

    return SELLER_STORE_PATH.equals(path);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CurrentUser currentUser = currentUserRender.read(authentication);

    RoleGuard.requireSeller(currentUser);

    try {
      StoreContext.where(sellerStoreProvider.resolveStoreId(currentUser)).call(() -> {
        filterChain.doFilter(request, response);
        return null;
      });
    } catch (ServletException | IOException e) {
      throw e;
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
