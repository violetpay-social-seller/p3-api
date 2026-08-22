package io.point3.p3api.auth.infrastructure.stomp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StompCurrentUserContext {

  public static final String HEADER = "p3.stomp.current-user";
}
