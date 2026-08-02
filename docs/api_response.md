# 공통 API 응답

API 응답은 `ApiResponse`로 감싼다.

성공 응답은 `data`를 사용한다.

실패 응답은 `error`를 사용한다.

`null` 값은 응답 JSON에서 제외된다.

---

## ApiResponse

공통 응답 최상위 구조.

```text
success: 요청 성공 여부
data: 성공 응답 데이터
error: 실패 응답 정보
```

성공 응답.

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "sample"
  }
}
```

데이터가 없는 성공 응답.

```json
{
  "success": true
}
```

실패 응답.

```json
{
  "success": false,
  "error": {
    "code": "COMMON_001",
    "type": "about:blank",
    "title": "Bad Request",
    "status": 400,
    "instance": "/api/sample"
  }
}
```

---

## ErrorResult

실패 응답의 상세 정보.

```text
code: 서비스 내부 에러 코드
type: 에러 타입 URI
title: 에러 제목
status: HTTP 상태 코드
instance: 에러가 발생한 요청 경로
```

`ErrorCode`와 요청 경로를 받아 생성한다.

---

## ErrorCode

에러 코드 인터페이스.

도메인별 에러 코드는 `ErrorCode`를 구현해서 정의한다.

```text
code: 내부 식별 코드
title: 클라이언트에 내려줄 기본 메시지
status: HTTP 상태
type: 에러 타입 URI
```

---

## 사용 방식

Controller는 반환 타입을 `ApiResponse<T>`로 둔다.

성공 시 데이터가 있으면 `ApiResponse.ok(data)`를 반환한다.

성공 시 데이터가 없으면 `ApiResponse.ok()`를 반환한다.

전역 예외 처리가 추가되면 예외를 `ErrorCode`로 변환한다.

변환한 `ErrorCode`와 요청 경로로 `ErrorResult`를 만든다.

최종 실패 응답은 `ApiResponse.fail(error)`로 반환한다.

---

## 적용 범위

일반 API 응답에 사용한다.

Validation 실패 응답도 같은 구조를 사용한다.

인증/인가 실패 응답도 같은 구조를 사용한다.

파일 업로드 응답도 같은 구조를 사용한다.

