package io.point3.p3api.inquiry.controller.request;

import jakarta.validation.constraints.NotBlank;

public record SendChatMessageRequest(@NotBlank String content) {}
