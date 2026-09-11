package io.point3.p3api.account.infrastructure.crypto;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AccountEncryptionProperties.class)
public class AccountEncryptionConfiguration {}
