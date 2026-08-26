package io.point3.p3api.operator.application.result;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResult<T>(List<T> items, int page, int size, long totalElements, int totalPages) {

  public PageResult {
    items = List.copyOf(items);
  }

  public static <T> PageResult<T> from(Page<T> page) {
    return new PageResult<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  @Override
  public List<T> items() {
    return List.copyOf(items);
  }
}
