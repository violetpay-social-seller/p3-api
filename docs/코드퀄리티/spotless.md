# Spotless

## 역할

Spotless는 코드 포맷을 자동으로 맞추는 도구임.

사람마다 들여쓰기, 공백, import 정리 방식이 달라지면 코드 리뷰에서 기능과 상관없는 변경이 많이 생김.
Spotless는 이런 포맷 차이를 자동으로 정리해서 리뷰에서 실제 로직에 집중할 수 있게 해줌.

이 프로젝트에서는 Java 파일과 Gradle Kotlin DSL 파일을 검사함.

## 현재 설정

현재 `build.gradle.kts` 기준 설정은 아래와 같음.

```kotlin
spotless {
    java {
        palantirJavaFormat().style("GOOGLE")
        target("src/**/*.java")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}
```

## 적용 대상

Java:

```text
src/**/*.java
```

Gradle Kotlin DSL:

```text
*.gradle.kts
```

즉, `src/main/java`, `src/test/java` 아래 Java 코드와 루트의 `build.gradle.kts`, `settings.gradle.kts` 같은 파일이 주요 대상임.

## 검사 명령

포맷이 맞는지만 확인하려면 아래 명령을 사용함.

```bash
./gradlew spotlessCheck
```

포맷이 맞지 않으면 빌드가 실패함.
이 명령은 파일을 수정하지 않고 문제만 알려줌.

## 자동 수정 명령

포맷을 자동으로 고치려면 아래 명령을 사용함.

```bash
./gradlew spotlessApply
```

자동 수정 후에는 변경된 파일을 확인하고 커밋하면 됨.

```bash
git diff
```

## 언제 쓰면 좋은가

- 커밋하기 전
- PR 올리기 전
- 코드 리뷰에서 포맷 지적이 나오기 전
- import 정리가 꼬였을 때
- 여러 사람이 작업하면서 포맷이 달라졌을 때

## 주의할 점

`spotlessApply`는 실제 파일을 수정함.
따라서 자동 수정 후 `git diff`로 의도하지 않은 변경이 있는지 확인해야 함.

포맷 변경과 기능 변경이 한 커밋에 너무 많이 섞이면 리뷰가 어려워짐.
가능하면 포맷 수정은 기능 커밋과 분리하는 게 좋음.

## 자주 쓰는 흐름

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
git diff
```

이 흐름으로 포맷을 먼저 정리하고, 변경사항을 확인한 뒤 커밋하면 됨.
