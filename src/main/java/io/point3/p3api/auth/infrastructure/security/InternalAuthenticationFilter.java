package io.point3.p3api.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.common.web.response.ErrorResult;
import io.point3.p3api.exception.code.CommonErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalAuthenticationFilter extends OncePerRequestFilter {

  private static final String INTERNAL_PATH_PREFIX = "/internal/";
  private static final String INTERNAL_TOKEN_HEADER = "X-P3-Internal-Token";
  private static final String LEGACY_INTERNAL_TOKEN_HEADER = "X-Internal-Api-Key";

  private final ObjectWriter responseWriter;
  private final String internalToken;

  public InternalAuthenticationFilter(
      ObjectMapper objectMapper, @Value("${p3.internal.auth.token:}") String internalToken) {
    this.responseWriter = objectMapper.writerFor(ApiResponse.class);
    this.internalToken = internalToken;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (!request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!matches(request.getHeader(INTERNAL_TOKEN_HEADER))
        && !matches(request.getHeader(LEGACY_INTERNAL_TOKEN_HEADER))) {
      SecurityContextHolder.clearContext();
      writeUnauthorized(request, response);
      return;
    }

    SecurityContextHolder.getContext().setAuthentication(internalAuthentication());
    filterChain.doFilter(request, response);
  }

  private boolean matches(String requestedToken) {
    if (internalToken == null || internalToken.isBlank() || requestedToken == null) {
      return false;
    }

    byte[] expected = internalToken.getBytes(StandardCharsets.UTF_8);
    byte[] actual = requestedToken.getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(expected, actual);
  }

  private PreAuthenticatedAuthenticationToken internalAuthentication() {
    return new PreAuthenticatedAuthenticationToken(
        "internal", "", List.of(new SimpleGrantedAuthority("ROLE_INTERNAL")));
  }

  private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    CommonErrorCode errorCode = CommonErrorCode.UNAUTHORIZED;
    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    responseWriter.writeValue(
        response.getWriter(), ApiResponse.fail(ErrorResult.of(errorCode, request.getRequestURI())));
  }
}
