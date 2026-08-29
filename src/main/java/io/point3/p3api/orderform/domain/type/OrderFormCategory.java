package io.point3.p3api.orderform.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderFormCategory {
  DESIGN("디자인", 0),
  SHAPE("모양", 1),
  CAKE_FLAVOR("케이크맛", 2),
  CAKE_DESIGN("케이크 디자인", 3),
  PACKAGING("포장 방식", 4),
  OTHER_REQUEST("기타 요청사항", 5);

  private final String title;
  private final int sortOrder;
}
