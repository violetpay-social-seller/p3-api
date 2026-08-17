package io.point3.p3api.inquiry.application.result;

import java.time.Instant;

public record OrderFormDraftResult(String draftKey, Instant expiresAt) {}
