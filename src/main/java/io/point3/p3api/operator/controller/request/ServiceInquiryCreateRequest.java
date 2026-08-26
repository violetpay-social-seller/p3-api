package io.point3.p3api.operator.controller.request;

import jakarta.validation.constraints.NotBlank;

public record ServiceInquiryCreateRequest(
    @NotBlank String title, @NotBlank String body) {}
