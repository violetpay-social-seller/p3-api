package io.point3.p3api.auth.controller;

import io.point3.p3api.auth.JwtCommandExtractor;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.user.application.profile.UpdateUserProfileCommand;
import io.point3.p3api.user.application.profile.UserProfileQueryUseCase;
import io.point3.p3api.user.application.profile.UserProfileUpdateUseCase;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.application.registration.UserRegistrationUseCase;
import io.point3.p3api.user.application.result.UserProfileResult;
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
  private final UserProfileQueryUseCase userProfileQueryUseCase;
  private final UserProfileUpdateUseCase userProfileUpdateUseCase;
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
        jwtCommandExtractor.extractRegistration(jwt, request.toRole(), request.phoneNumber());
    UserSyncResult userSyncResult = userRegistrationUseCase.completeRegistration(command);
    return ApiResponse.ok(UserSyncResponse.from(userSyncResult));
  }

  @GetMapping("/me")
  public ApiResponse<UserProfileResponse> getProfile(@Authenticated CurrentUser currentUser) {
    UserProfileResult result = userProfileQueryUseCase.getProfile(currentUser.userId());

    return ApiResponse.ok(UserProfileResponse.from(result));
  }

  @PatchMapping("/me")
  public ApiResponse<UserProfileResponse> updateProfile(
      @Authenticated CurrentUser currentUser,
      @Valid @RequestBody UserProfileUpdateRequest request) {
    UserProfileResult result = userProfileUpdateUseCase.updateProfile(
        UpdateUserProfileCommand.of(currentUser.userId(), request.email(), request.name()));

    return ApiResponse.ok(UserProfileResponse.from(result));
  }
}
