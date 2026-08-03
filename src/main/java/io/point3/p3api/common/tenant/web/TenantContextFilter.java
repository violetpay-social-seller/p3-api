package io.point3.p3api.common.tenant.web;

import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.access.TenantAccessChecker;
import io.point3.p3api.common.tenant.context.TenantContext;
import io.point3.p3api.common.tenant.resolvers.TenantResolver;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    private final CurrentUserRender currentUserRender;
    private final TenantResolver<HttpServletRequest> tenantResolver;
    private final TenantAccessChecker tenantAccessChecker;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String tenantId = tenantResolver.resolveTenantIdentifier(request)
                .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_INPUT));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CurrentUser currentUser = currentUserRender.read(authentication);

        tenantAccessChecker.check(currentUser, tenantId);

        try {
            TenantContext.where(tenantId).call(() -> {
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
