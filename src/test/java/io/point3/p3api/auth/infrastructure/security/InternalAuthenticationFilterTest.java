package io.point3.p3api.auth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class InternalAuthenticationFilterTest {

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("internal 경로는 shared token이 맞을 때 인증을 세팅한다")
  void authenticatesInternalRequest() throws ServletException, IOException {
    InternalAuthenticationFilter filter =
        new InternalAuthenticationFilter(new ObjectMapper(), "secret");
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/internal/assets/id/variants");
    request.addHeader("X-P3-Internal-Token", "secret");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertEquals(200, response.getStatus());
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(
        "internal", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  @Test
  @DisplayName("internal 경로는 shared token이 없으면 401로 거절한다")
  void rejectsMissingInternalToken() throws ServletException, IOException {
    InternalAuthenticationFilter filter =
        new InternalAuthenticationFilter(new ObjectMapper(), "secret");
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/internal/assets/id/variants");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertEquals(401, response.getStatus());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
