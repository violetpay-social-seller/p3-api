package io.point3.p3api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class P3ApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
