package io.point3.p3api.common.tenant.seller.filter;

import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
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
    return !path.startsWith(SELLER_PREFIX)
        || ("POST".equals(request.getMethod()) && SELLER_STORE_PATH.equals(path));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CurrentUser currentUser = currentUserRender.read(authentication);

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
