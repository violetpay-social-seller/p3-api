package io.point3.p3api.store.infrastructure.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StoreWebProperties.class)
public class StoreWebConfiguration {}
