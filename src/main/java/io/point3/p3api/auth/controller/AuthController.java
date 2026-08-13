package io.point3.p3api.auth.controller;

import io.point3.p3api.auth.JwtSyncCommandExtractor;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.user.application.result.UserSyncResult;
import io.point3.p3api.user.application.sync.SyncCommand;
import io.point3.p3api.user.application.sync.UserSyncUseCase;
import io.point3.p3api.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserSyncUseCase userSyncUseCase;
  private final JwtSyncCommandExtractor jwtSyncCommandExtractor;

  @PostMapping("/me/sync")
  public ApiResponse<UserSyncResponse> sync(
          @AuthenticationPrincipal Jwt jwt
          ) {
    SyncCommand command = jwtSyncCommandExtractor.extract(jwt);
    UserSyncResult userSyncResult = userSyncUseCase.sync(command);
    return ApiResponse.ok(UserSyncResponse.from(userSyncResult));
  }
}