# 권장 사용 흐름

## 기본 원칙

코드퀄리티 도구는 각각 보는 관점이 다름.

```text
Spotless  -> 코드 모양
SpotBugs  -> 잠재 버그
JaCoCo    -> 테스트가 닿은 범위
```

따라서 하나만 통과했다고 코드 품질이 보장되는 것은 아님.
세 도구를 역할별로 나눠서 사용하는 게 좋음.

## 커밋 전 최소 흐름

가장 가볍게 확인하려면 아래 순서로 가면 됨.

```bash
./gradlew spotlessApply
./gradlew check
```

의미는 아래와 같음.

1. `spotlessApply`로 포맷을 자동 정리함
2. `check`로 테스트, 포맷 검사, 정적 분석을 확인함

이후 변경사항을 확인함.

```bash
git diff
```

## PR 전 권장 흐름

PR 올리기 전에는 커버리지 리포트까지 보는 게 좋음.

```bash
./gradlew spotlessApply
./gradlew check
./gradlew jacocoTestReport
```

그 다음 HTML 리포트를 확인함.

```text
build/reports/jacoco/test/html/index.html
build/reports/spotbugs/main.html
```

## 실패했을 때 처리 순서

### 1. Spotless 실패

대부분 자동 수정 가능함.

```bash
./gradlew spotlessApply
```

수정 후 다시 확인함.

```bash
./gradlew spotlessCheck
```

### 2. 테스트 실패

테스트 실패는 포맷이나 정적 분석보다 먼저 봐야 함.
기능이 깨졌을 가능성이 가장 높기 때문임.

```bash
./gradlew test
```

### 3. SpotBugs 실패

HTML 리포트를 확인함.

```text
build/reports/spotbugs/main.html
```

실제 버그인지 오탐인지 판단하고 수정함.

### 4. JaCoCo 커버리지 확인

현재는 커버리지 부족으로 빌드가 실패하지 않음.
그래서 실패 처리보다는 테스트 보강 기준으로 사용하면 됨.

```text
build/reports/jacoco/test/html/index.html
```

## 추천 습관

기능 구현 중에는 너무 자주 전체 검사를 돌릴 필요 없음.
대신 작업 단위가 끝날 때 아래 명령을 습관처럼 돌리면 됨.

```bash
./gradlew check
```

포맷이 자주 깨지는 편이면 저장 후 또는 커밋 전에 아래 명령을 먼저 실행하면 됨.

```bash
./gradlew spotlessApply
```

테스트를 추가했거나 리팩터링을 크게 했다면 JaCoCo 리포트까지 확인함.

```bash
./gradlew test jacocoTestReport
```

## CI에 넣을 때

CI에서는 보통 아래 순서면 충분함.

```bash
./gradlew check jacocoTestReport
```

CI가 실패하면 아래 순서로 원인을 보면 됨.

1. 테스트 실패인지 확인
2. Spotless 실패인지 확인
3. SpotBugs 실패인지 확인
4. JaCoCo 리포트는 테스트 보강 판단에 사용

## 과하게 쓰지 말아야 하는 부분

초반부터 커버리지 기준을 너무 높게 잡으면 테스트 품질보다 숫자 맞추기에 끌려갈 수 있음.

추천 방향은 아래와 같음.

- 전체 커버리지 숫자를 무리하게 올리지 않음
- 핵심 서비스 로직부터 테스트를 보강함
- SpotBugs 경고는 가능한 한 코드 수정으로 해결함
- Spotless는 고민하지 말고 자동화함
