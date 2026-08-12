package io.point3.p3api.common.web.response;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.ErrorCode;
import io.point3.p3api.exception.code.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ApiResponse<Void>> handleAllExceptions(
      BaseException e, HttpServletRequest request) {
    ErrorCode errorCode = e.getErrorCode();

    String instance = request.getRequestURI();

    log.warn(
        "Business exception. code={} ,title={} ,status={} ,type={}",
        errorCode.getCode(),
        errorCode.getTitle(),
        errorCode.getStatus(),
        errorCode.getType());
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(ErrorResult.of(errorCode, instance)));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
    String instance = request.getRequestURI();

    log.warn(
        "Validation exception. code={} ,title={} ,status={} ,type={}",
        errorCode.getCode(),
        errorCode.getTitle(),
        errorCode.getStatus(),
        errorCode.getType());

    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(ErrorResult.of(errorCode, instance)));
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataAccessException(
      DataAccessException e, HttpServletRequest request) {
    ErrorCode errorCode = CommonErrorCode.DATA_ACCESS_ERROR;
    String instance = request.getRequestURI();

    log.error(
        "Data access exception. code={} ,title={} ,status={} ,type={}",
        errorCode.getCode(),
        errorCode.getTitle(),
        errorCode.getStatus(),
        errorCode.getType(),
        e);

    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(ErrorResult.of(errorCode, instance)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
      Exception e, HttpServletRequest request) {
    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
    String instance = request.getRequestURI();

    log.error(
        "Unexpected exception. code={} ,title={} ,status={} ,type={}",
        errorCode.getCode(),
        errorCode.getTitle(),
        errorCode.getStatus(),
        errorCode.getType(),
        e);

    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.fail(ErrorResult.of(errorCode, instance)));
  }
}
