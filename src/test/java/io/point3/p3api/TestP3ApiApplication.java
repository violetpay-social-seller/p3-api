package io.point3.p3api;

import org.springframework.boot.SpringApplication;

public class TestP3ApiApplication {

  public static void main(String[] args) {
    SpringApplication.from(P3ApiApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
