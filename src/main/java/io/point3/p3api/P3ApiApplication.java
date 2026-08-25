package io.point3.p3api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class P3ApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(P3ApiApplication.class, args);
  }
}
