# SpotBugs

## 역할

SpotBugs는 Java bytecode를 분석해서 잠재적인 버그를 찾아주는 정적 분석 도구임.

테스트는 실제 실행한 케이스만 검증하지만, SpotBugs는 코드 패턴을 보고 위험한 부분을 찾아냄.
예를 들면 null 처리 실수, 잘못된 equals/hashCode 구현, 리소스 누수 가능성, 동시성 문제 같은 것들을 잡는 데 도움됨.

## 현재 설정

현재 `build.gradle.kts` 기준 설정은 아래와 같음.

```kotlin
spotbugs {
    ignoreFailures.set(false)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
}
```

의미는 아래와 같음.

| 설정 | 의미 |
| --- | --- |
| `ignoreFailures = false` | 버그가 발견되면 빌드를 실패시킴 |
| `Effort.MAX` | 분석 강도를 높게 가져감 |
| `Confidence.MEDIUM` | 신뢰도 중간 이상 이슈를 보고함 |

리포트 설정은 아래와 같음.

```kotlin
tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports {
        create("html") {
            required.set(true)
        }

        create("xml") {
            required.set(false)
        }
    }
}
```

HTML 리포트는 생성하고, XML 리포트는 생성하지 않음.

## 실행 명령

메인 코드만 분석하려면 아래 명령을 사용함.

```bash
./gradlew spotbugsMain
```

테스트 코드까지 분석하려면 아래 명령을 사용함.

```bash
./gradlew spotbugsTest
```

전체 검증 흐름에서 함께 돌리고 싶으면 아래 명령을 사용함.

```bash
./gradlew check
```

## 리포트 위치

실행 후 HTML 리포트는 보통 아래 경로에 생성됨.

```text
build/reports/spotbugs/main.html
build/reports/spotbugs/test.html
```

정확한 파일명은 실행한 task에 따라 달라질 수 있으니 `build/reports/spotbugs` 아래를 확인하면 됨.

## 언제 쓰면 좋은가

- 기능 구현 후 잠재 버그를 확인할 때
- 테스트는 통과하지만 코드가 찜찜할 때
- null 처리나 equals/hashCode 쪽을 건드렸을 때
- 리팩터링 후 의도치 않은 위험 패턴이 생겼는지 확인할 때
- CI에서 최소 품질 기준을 강제하고 싶을 때

## 실패했을 때 보는 법

SpotBugs가 실패하면 바로 코드를 고치기보다 HTML 리포트를 먼저 보는 게 좋음.

```text
build/reports/spotbugs/main.html
```

리포트에서 확인할 것:

- 어떤 클래스에서 발생했는지
- 어떤 라인 근처인지
- 버그 카테고리가 무엇인지
- 실제 위험인지, 오탐인지

오탐일 수도 있지만, 처음에는 억지로 무시하지 말고 왜 잡혔는지 먼저 이해하는 게 좋음.

## 주의할 점

SpotBugs는 정적 분석 도구라서 모든 경고가 실제 버그는 아님.
하지만 현재 설정은 `ignoreFailures = false`라서 경고가 빌드 실패로 이어짐.

따라서 경고가 나오면 아래 순서로 판단하면 됨.

1. 실제 버그인지 확인함
2. 실제 버그면 코드 수정함
3. 오탐이면 왜 오탐인지 근거를 남기고 억제 방법을 고민함

초반에는 오탐 억제보다 코드 수정으로 해결하는 쪽이 더 안전함.
