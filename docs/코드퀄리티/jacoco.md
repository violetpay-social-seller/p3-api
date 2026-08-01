# JaCoCo

## 역할

JaCoCo는 테스트 커버리지를 측정하는 도구임.

테스트가 얼마나 많은 코드를 실행했는지 수치와 HTML 리포트로 보여줌.
커버리지가 높다고 좋은 테스트가 보장되는 것은 아니지만, 테스트가 아예 닿지 않는 영역을 찾는 데는 매우 유용함.

## 현재 설정

현재 `build.gradle.kts` 기준 설정은 아래와 같음.

```kotlin
jacoco {
    toolVersion = "0.8.13"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
    }
}
```

의미는 아래와 같음.

| 설정 | 의미 |
| --- | --- |
| `toolVersion = 0.8.13` | JaCoCo 버전 고정 |
| `test.finalizedBy(jacocoTestReport)` | 테스트 실행 후 커버리지 리포트 생성 |
| `html = true` | 사람이 보기 좋은 HTML 리포트 생성 |
| `xml = true` | CI나 외부 도구 연동용 XML 생성 |
| `csv = true` | 데이터 분석용 CSV 생성 |

## 실행 명령

테스트와 커버리지 리포트를 함께 생성하려면 아래 명령을 사용함.

```bash
./gradlew test jacocoTestReport
```

현재 설정상 `test` 실행 후 `jacocoTestReport`가 이어서 실행되도록 연결되어 있음.
그래도 리포트를 명확히 만들고 싶으면 위처럼 명시적으로 실행하면 됨.

## 리포트 위치

HTML 리포트:

```text
build/reports/jacoco/test/html/index.html
```

XML 리포트:

```text
build/reports/jacoco/test/jacocoTestReport.xml
```

CSV 리포트:

```text
build/reports/jacoco/test/jacocoTestReport.csv
```

보통 사람이 확인할 때는 HTML 리포트를 보면 됨.

## 언제 쓰면 좋은가

- 테스트를 추가한 뒤 실제로 어느 코드가 검증됐는지 볼 때
- 리팩터링 전에 테스트 보호 범위를 확인할 때
- 중요한 서비스 로직이나 도메인 로직이 테스트에서 빠져있는지 확인할 때
- PR에서 테스트 보강 근거를 남기고 싶을 때

## 커버리지 해석 방법

커버리지는 숫자만 높다고 좋은 게 아님.

중요한 기준은 아래에 가까움.

- 핵심 비즈니스 로직이 테스트되고 있는가
- 예외 상황이 테스트되고 있는가
- 조건 분기가 테스트되고 있는가
- 단순 getter/setter 같은 의미 없는 코드로 숫자만 올린 것은 아닌가

즉, 커버리지는 목표가 아니라 점검 도구임.

## 현재 한계

현재 설정에는 `jacocoTestCoverageVerification` 기준이 없음.
따라서 커버리지가 낮아도 빌드가 실패하지 않음.

나중에 최소 커버리지 기준을 강제하고 싶으면 아래 같은 방향으로 별도 설정을 추가하면 됨.

```text
jacocoTestCoverageVerification
```

다만 초반에는 무리하게 전체 커버리지 기준을 높게 잡기보다, 핵심 도메인이나 서비스 단위부터 기준을 잡는 게 좋음.
