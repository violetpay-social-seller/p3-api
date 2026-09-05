package io.point3.p3api.common.web.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.point3.p3api.exception.code.CommonErrorCode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("읽을 수 없는 요청 본문은 잘못된 입력으로 응답한다")
  void handlesUnreadableMessageAsInvalidInput() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/stores/p3-seed-cake/order-form-drafts");

    ResponseEntity<ApiResponse<Void>> response = handler.handleUnreadableMessageException(
        new HttpMessageNotReadableException("invalid request body", new EmptyInputMessage()),
        request);
    ApiResponse<Void> body = response.getBody();

    assertEquals(CommonErrorCode.INVALID_INPUT.getStatus(), response.getStatusCode());
    assertNotNull(body);
    assertFalse(body.success());
    assertEquals(CommonErrorCode.INVALID_INPUT.getCode(), body.error().code());
  }

  private static class EmptyInputMessage implements HttpInputMessage {

    @Override
    public InputStream getBody() {
      return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public HttpHeaders getHeaders() {
      return HttpHeaders.EMPTY;
    }
  }
}
