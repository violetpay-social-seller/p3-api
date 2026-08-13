package io.point3.p3api.auth.controller;

import io.point3.p3api.auth.JwtCommandExtractor;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.application.registration.UserRegistrationUseCase;
import io.point3.p3api.user.application.result.UserSyncResult;
import io.point3.p3api.user.application.sync.SyncCommand;
import io.point3.p3api.user.application.sync.UserSyncUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserSyncUseCase userSyncUseCase;
  private final UserRegistrationUseCase userRegistrationUseCase;
  private final JwtCommandExtractor jwtCommandExtractor;

  @PostMapping("/me/sync")
  public ApiResponse<UserSyncResponse> sync(@AuthenticationPrincipal Jwt jwt) {
    SyncCommand command = jwtCommandExtractor.extractSync(jwt);
    UserSyncResult userSyncResult = userSyncUseCase.sync(command);
    return ApiResponse.ok(UserSyncResponse.from(userSyncResult));
  }

  @PostMapping("/me/registration")
  public ApiResponse<UserSyncResponse> completeRegistration(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CompleteRegistrationRequest request) {
    CompleteRegistrationCommand command =
        jwtCommandExtractor.extractRegistration(jwt, request.toRole());
    UserSyncResult userSyncResult = userRegistrationUseCase.completeRegistration(command);
    return ApiResponse.ok(UserSyncResponse.from(userSyncResult));
  }
}
