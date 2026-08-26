package io.point3.p3api.operator.application.query;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record OperatorPageQuery(Integer page, Integer size) {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 20;
  private static final int MAX_SIZE = 100;

  public Pageable toPageable() {
    int resolvedPage = page == null ? DEFAULT_PAGE : page;
    int resolvedSize = size == null ? DEFAULT_SIZE : size;
    if (resolvedPage < 0 || resolvedSize < 1 || resolvedSize > MAX_SIZE) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Invalid page query");
    }

    return PageRequest.of(resolvedPage, resolvedSize);
  }
}
