package io.point3.p3api.store.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "p3.web")
public record StoreWebProperties(@NotBlank String baseUrl) {}
