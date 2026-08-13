package io.point3.p3api.common.tenant.buyer.filter;

import io.point3.p3api.common.tenant.buyer.provider.PublicStoreProvider;
import io.point3.p3api.common.tenant.context.StoreContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class PublicStoreContextFilter extends OncePerRequestFilter {

  private static final String STORE_PREFIX = "/stores/";

  private final PublicStoreProvider publicStoreProvider;

  public PublicStoreContextFilter(PublicStoreProvider publicStoreProvider) {
    this.publicStoreProvider = publicStoreProvider;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith(STORE_PREFIX);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String slug = extractSlug(request.getRequestURI());

    try {
      StoreContext.where(publicStoreProvider.resolveStoreId(slug)).call(() -> {
        filterChain.doFilter(request, response);
        return null;
      });
    } catch (ServletException | IOException e) {
      throw e;
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  private String extractSlug(String uri) {
    String path = uri.substring(STORE_PREFIX.length());
    int slashIndex = path.indexOf("/");

    if (slashIndex < 0) {
      return path;
    }

    return path.substring(0, slashIndex);
  }
}
