# 6주차 API 명세 — 회원가입·평점 등록/수정/삭제

현재 구현 코드 기준으로 작성한 명세다. 응답 예시는 설명용이며 실제 요청·응답 캡처가 아니다.

## 공통 사항

- 로컬 기본 URL: `http://localhost:8080` (서버 포트를 별도로 바꿨다면 변경)
- POST·PATCH 요청: `Content-Type: application/json`
- 인증: 현재 로그인 도입 전 단계. 회원 ID를 URL에서 받는다.
- `memberId`, `ratingId`는 실제 DB에 존재하는 ID를 사용한다.
- 성공·실패 JSON 형식은 다음과 같다. 단, DELETE 성공은 HTTP 204이며 본문이 없다.
- JSON의 `code`는 서비스 결과 코드로, 실제 HTTP 상태 코드와 별개의 값이다.

```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "처리에 성공했습니다.",
  "result": {}
}
```

| 기능 | Method | 경로 | 성공 상태 | result |
|---|---|---|---|---|
| 회원가입 | POST | `/members` | 201 Created | 회원 ID·이메일·닉네임 |
| 평점 등록 | POST | `/members/{memberId}/ratings` | 201 Created | 생성된 평점 ID |
| 평점 수정 | PATCH | `/members/{memberId}/ratings/{ratingId}` | 200 OK | null |
| 평점 삭제 | DELETE | `/members/{memberId}/ratings/{ratingId}` | 204 No Content | 본문 없음 |

## 1. 회원가입

`POST /members`

| 필드 | 타입 | 필수 | 규칙 |
|---|---|---|---|
| email | string | O | 공백만 입력 불가, 이메일 형식, 최대 254자, 중복 불가 |
| password | string | O | 8자 이상, 영문 대문자·소문자·숫자·`!@#$%^&*` 중 하나 이상 포함, UTF-8 기준 최대 72바이트 |
| nickname | string | O | 공백만 입력 불가, 2~12자, 중복 불가 |

위 비밀번호 특수문자 집합은 반드시 하나 이상 포함해야 하는 집합이며, 그 외 문자를 모두 금지하는 규칙은 아니다.
비밀번호는 bcrypt 해시로 저장하며, 응답에 평문·해시 모두 포함하지 않는다.

요청 예시:

```json
{
  "email": "missiona@example.com",
  "password": "Mission1!",
  "nickname": "미션회원A"
}
```

성공 응답 예시 — HTTP 201:

```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "회원가입에 성공했습니다.",
  "result": {
    "memberId": 1,
    "email": "missiona@example.com",
    "nickname": "미션회원A"
  }
}
```

| 실패 조건 | HTTP | code | message 예시 |
|---|---|---|---|
| DTO 입력 규칙 위반 | 400 | COMMON4001 | 올바른 이메일 형식이 아닙니다. |
| 비밀번호가 UTF-8 기준 72바이트 초과 | 400 | COMMON400 | 비밀번호는 UTF-8 기준 72바이트 이하로 입력해주세요. |
| 이메일 중복 | 409 | COMMON4092 | 이미 사용 중인 이메일입니다. |
| 닉네임 중복 | 409 | COMMON4092 | 이미 사용 중인 닉네임입니다. |
| DB 제약 위반 | 409 | COMMON4091 | 이미 존재하는 데이터입니다. |

## 2. 평점 등록

`POST /members/{memberId}/ratings`

- Path: `memberId` — 작성할 회원 ID (Long)

| JSON 필드 | 타입 | 필수 | 규칙 |
|---|---|---|---|
| movieId | integer (Long) | O | 0보다 큰 TMDB 영화 번호 |
| score | integer | O | 1~5 |
| comment | string | X | 최대 500자, 생략·null 허용 |

`movieId`는 우리 DB의 Movie 테이블을 참조하지 않는다. 양수 여부를 검사하며 TMDB에 실제 존재하는 영화인지는 검증하지 않는다.
같은 회원은 같은 영화에 한 번만 등록할 수 있다. 서비스 사전 확인과 DB의 `(member_id, movie_id)` 복합 UNIQUE 제약을 함께 사용한다.

요청 예시:

```json
{
  "movieId": 550,
  "score": 5,
  "comment": "재밌게 봤습니다."
}
```

성공 응답 예시 — HTTP 201 (`result`는 객체가 아니라 평점 ID 숫자):

```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "평점 등록에 성공했습니다.",
  "result": 10
}
```

| 실패 조건 | HTTP | code | message 예시 |
|---|---|---|---|
| 입력 규칙 위반 | 400 | COMMON4001 | 별점은 5점 이하여야 합니다. |
| 존재하지 않는 회원 | 404 | COMMON404 | 존재하지 않는 유저입니다. |
| 같은 회원·영화 조합의 평점 존재 | 409 | COMMON4092 | 이미 이 영화에 평점을 남기셨습니다. |
| DB 제약 위반 (동시 중복 등록 등) | 409 | COMMON4091 | 이미 존재하는 데이터입니다. |

## 3. 평점 수정

`PATCH /members/{memberId}/ratings/{ratingId}`

- Path: `memberId` — 요청 회원 ID (Long), `ratingId` — 수정할 평점 ID (Long)
- 회원 번호나 영화 번호를 변경하는 API가 아니라 별점·후기를 변경하는 API다.

| JSON 필드 | 타입 | 필수 | 규칙 |
|---|---|---|---|
| score | integer | O | 1~5 |
| comment | string | X | 최대 500자 |

현재 구현에서 PATCH여도 score는 필수다. comment를 생략하거나 null로 보내면 기존 후기를 유지하는 것이 아니라 null로 변경한다.

요청 예시:

```json
{
  "score": 4,
  "comment": "다시 보니 더 좋았습니다."
}
```

성공 응답 — HTTP 200:

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "평점 수정에 성공했습니다.",
  "result": null
}
```

| 실패 조건 | HTTP | code | message 예시 |
|---|---|---|---|
| 입력 규칙 위반 | 400 | COMMON4001 | 별점은 1점 이상이어야 합니다. |
| 평점 없음 | 404 | COMMON404 | 존재하지 않는 평점입니다. |
| 평점 작성자 ID와 요청 memberId가 다름 | 403 | COMMON403 | 본인이 작성한 평점만 수정하거나 삭제할 수 있습니다. |

## 4. 평점 삭제

`DELETE /members/{memberId}/ratings/{ratingId}`

- Path: `memberId` — 요청 회원 ID (Long), `ratingId` — 삭제할 평점 ID (Long)
- 요청 본문 없음.
- 성공: HTTP **204 No Content**, 응답 본문 없음. `{}`나 `ApiResponse` JSON을 보내지 않는다.
- 평점 없음: HTTP 404 / `COMMON404` / `존재하지 않는 평점입니다.`
- 작성자 ID와 memberId 불일치: HTTP 403 / `COMMON403` / `본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.`

## 공통 실패 응답 예시

HTTP 409:

```json
{
  "isSuccess": false,
  "code": "COMMON4092",
  "message": "이미 이 영화에 평점을 남기셨습니다.",
  "result": null
}
```

- DTO 검증 실패 시 첫 번째 필드 오류 메시지 하나를 반환한다. 여러 필드가 잘못되면 어떤 메시지가 먼저 선택될지는 고정되지 않는다.
- 위 표는 현재 구현한 입력 검증·서비스·DB 제약 예외의 계약이다. 모든 종류의 HTTP 오류를 이 형태로 통일한 것은 아니다.
- 403 소유자 검사는 URL memberId와 저장된 작성자 ID의 비교다. 로그인 인증을 수행하지 않으므로 실제 호출자의 신원을 보장하지 않는다. 타인의 ID를 아는 요청까지 막는 인증은 이후 구현 대상이다.

## 확인용 기존 조회 API

`GET /members/{memberId}/ratings` — 해당 회원 평점 조회 (HTTP 200).
공통 응답의 `result` 배열에 `ratingId`, `movieId`, `score`, `comment`, `createdAt`, `updatedAt`을 반환한다.
목록은 평점 ID 내림차순이며, 평점이 없으면 `result: []`다. 회원 자체가 없으면 HTTP 404다.
등록·수정·삭제 후 이 API로 실제 결과를 확인할 수 있다.
