package io.point3.p3api.common.tenant.web;

import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.provider.SellerStoreProvider;
import io.point3.p3api.common.tenant.context.StoreContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Provider <-> Context 연결 역할
 * 해당 Filter 판매자 스토어가 이미 있어야 하는 API만 적용되어야함
 */
@RequiredArgsConstructor
public class StoreContextFilter extends OncePerRequestFilter {

    private final CurrentUserRender currentUserRender;
    private final SellerStoreProvider sellerStoreProvider;

    /**
     * URI가 seller로 시작해야 필터 적용
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
            throws ServletException {
        return !request.getRequestURI().startsWith("/seller/");
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
