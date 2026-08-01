# 코드퀄리티 도구 정리

이 프로젝트는 Gradle 기준으로 `Spotless`, `SpotBugs`, `JaCoCo`를 사용함.

각 도구의 역할은 다름.

| 도구 | 역할 | 주 사용 시점 |
| --- | --- | --- |
| Spotless | 코드 포맷과 import 정리 | 커밋 전, PR 전 |
| SpotBugs | 정적 분석으로 잠재 버그 탐지 | 기능 구현 후, CI 단계 |
| JaCoCo | 테스트 커버리지 리포트 생성 | 테스트 보강 후, 리팩터링 전후 |

## 빠른 실행

전체 품질 검사를 한 번에 확인하려면 아래 명령을 사용하면 됨.

```bash
./gradlew check
```

현재 설정에서는 `check` 실행 시 테스트, Spotless 검사, SpotBugs 검사 등이 함께 수행될 수 있음.
다만 JaCoCo는 커버리지 리포트를 생성하는 도구이고, 현재 설정에는 커버리지 기준 미달 시 빌드를 실패시키는 규칙은 없음.

테스트와 커버리지 리포트를 명시적으로 보고 싶으면 아래처럼 실행하면 됨.

```bash
./gradlew test jacocoTestReport
```

## 문서 목록

- [Spotless](./spotless.md)
- [SpotBugs](./spotbugs.md)
- [JaCoCo](./jacoco.md)
- [권장 사용 흐름](./quality-workflow.md)

## 현재 Gradle 설정 위치

설정은 루트 `build.gradle.kts`에 있음.

```text
plugins {
    id("com.diffplug.spotless")
    id("com.github.spotbugs")
    jacoco
}
```

도구 설정을 바꾸고 싶으면 새 설정 파일을 따로 찾기보다 먼저 `build.gradle.kts`를 확인하면 됨.
