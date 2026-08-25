package io.point3.p3api;

import java.util.UUID;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public abstract class IntegrationTestSupport {

  protected String uniqueEmail(String prefix) {
    return prefix + "-" + UUID.randomUUID() + "@example.test";
  }
}
