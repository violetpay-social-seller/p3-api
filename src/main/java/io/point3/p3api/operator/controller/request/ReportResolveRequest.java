package io.point3.p3api.operator.controller.request;

import io.point3.p3api.operator.domain.type.ReportStatus;
import io.point3.p3api.operator.domain.type.ReportTargetAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportResolveRequest(
    @NotNull ReportStatus status, @NotBlank String resolution, ReportTargetAction targetAction) {}
