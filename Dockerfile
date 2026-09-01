FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080

USER 65532:65532

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/app.jar"]
