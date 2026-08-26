package io.point3.p3api.operator.controller.request;

import jakarta.validation.constraints.NotBlank;

public record ServiceInquiryAnswerRequest(@NotBlank String answer) {}
