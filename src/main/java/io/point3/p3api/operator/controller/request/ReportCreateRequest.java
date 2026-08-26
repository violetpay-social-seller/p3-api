package io.point3.p3api.operator.controller.request;

import io.point3.p3api.operator.domain.type.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReportCreateRequest(
    @NotNull ReportTargetType targetType,
    @NotNull UUID targetId,
    @NotBlank String reason,
    String evidence) {}
