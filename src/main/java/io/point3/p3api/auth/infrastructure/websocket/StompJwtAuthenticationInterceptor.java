package io.point3.p3api.auth.infrastructure.websocket;

import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompJwtAuthenticationInterceptor implements ChannelInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtDecoder jwtDecoder;
  private final CurrentUserRender currentUserRender;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      accessor.setUser(authenticate(message, accessor));
    }

    return message;
  }

  private Authentication authenticate(Message<?> message, StompHeaderAccessor accessor) {
    String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

    if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
      throw unauthorized(message, null);
    }

    String accessToken = authorization.substring(BEARER_PREFIX.length());
    if (accessToken.isBlank()) {
      throw unauthorized(message, null);
    }

    try {
      Jwt jwt = jwtDecoder.decode(accessToken);
      Authentication authentication = new JwtAuthenticationToken(jwt);
      currentUserRender.read(authentication);
      return authentication;
    } catch (JwtException | BaseException e) {
      throw unauthorized(message, e);
    }
  }

  private MessageDeliveryException unauthorized(Message<?> message, Throwable cause) {
    return new MessageDeliveryException(
        message, "STOMP CONNECT requires a valid Bearer token", cause);
  }
}
