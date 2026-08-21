package io.point3.p3api.seller.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.seller.application.create.CreateSellerOnboardingCommand;
import io.point3.p3api.seller.application.create.SellerOnboardingCreateUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.controller.request.SellerOnboardingCreateRequest;
import io.point3.p3api.seller.controller.response.SellerOnboardingResponse;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/applications")
@RequiredArgsConstructor
public class SellerOnboardingController {

  private final SellerOnboardingCreateUseCase sellerOnboardingCreateUseCase;
  private final ObjectMapper objectMapper;

  @PostMapping
  public ApiResponse<SellerOnboardingResponse> create(
      @Authenticated CurrentUser currentUser,
      @Valid @RequestBody SellerOnboardingCreateRequest request) {
    RoleGuard.requireSeller(currentUser);

    SellerOnboardingResult result = sellerOnboardingCreateUseCase.create(
        CreateSellerOnboardingCommand.from(
            currentUser.userId(),
            request.storeName(),
            request.phoneNumber(),
            request.address(),
            serializeSnsLinks(request.snsLinks())));

    return ApiResponse.ok(SellerOnboardingResponse.from(result));
  }

  private String serializeSnsLinks(List<String> snsLinks) {
    if (snsLinks == null || snsLinks.isEmpty()) {
      return null;
    }

    try {
      return objectMapper.writeValueAsString(snsLinks);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
