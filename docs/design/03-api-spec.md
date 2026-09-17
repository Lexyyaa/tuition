# API 명세

> 아래 두 문서를 HTTP 계약으로 옮긴다.
> - 요구사항([01-requirements.md](01-requirements.md))
> - 도메인([02-domain-model.md](02-domain-model.md))
>
> 도메인 에러 코드도 여기서 관리한다.

---

## 1. 공통 규칙

- Base URL: `http://localhost:8080`
- Content-Type: `application/json`
- 시간: ISO-8601, `Asia/Seoul` (예: `2026-07-01T14:30:00`)
- 날짜: `yyyy-MM-dd` · 고지 월: `yyyy-MM`
- 금액: 정수(원 단위)
- 식별 헤더: `X-Academy-Id` (D-32)
  - 인증은 범위 밖 — 헤더로 학원을 식별만 한다
  - 누락 400 · 존재하지 않는 학원 404 · 타 학원 리소스 404 (존재 비노출)
  - 예외: API-1(학원 등록) · API-10(웹훅 수신)은 헤더 불요
- 멱등 헤더: `Idempotency-Key` (UUID, 납부 API-11에만, D-21)

**에러 응답**

```json
{ "errorCode": "ERROR_CODE", "message": "설명" }
```

**상태코드 기준**

| 상태 | 쓰는 경우 |
|---|---|
| 200 | 조회 · 처리 성공 · 멱등 재요청 |
| 201 | 신규 생성 |
| 202 | 접수 (발송은 비동기로 진행) |
| 400 | 입력 형식 오류 · 비즈니스 규칙 위반 |
| 404 | 리소스 없음 · 타 학원 리소스 (비노출) |
| 409 | 현재 상태에서 불가능한 요청 · 중복 |
| 422 | 멱등 키 재사용 — 같은 키 · 다른 본문 (D-22) |
| 500 | 서버 결함만 (입력 오류가 500으로 나가면 버그다) |

---

## 2. API 목록

| ID | 액터 | 유즈케이스 | Method | Path | FR |
|---|---|---|---|---|---|
| API-1 | 운영자 | 학원 등록 | POST | `/academies` | FR-2.12 |
| API-2 | 원장 | 학부모 등록 (수강생 포함) | POST | `/parents` | FR-2.10 · FR-2.12 |
| API-3 | 원장 | 수신 거부 변경 | PATCH | `/parents/{parentId}/notification-refusal` | FR-2.11 |
| API-4 | 원장 | 강좌 등록 | POST | `/courses` | FR-2.4 · FR-2.5 |
| API-5 | 원장 | 강좌 조회 | GET | `/courses/{courseId}` | FR-2.1 · FR-2.2 |
| API-6 | 원장 | 수강 등록 | POST | `/enrollments` | FR-2.6 · FR-2.8 · FR-2.9 |
| API-7 | 원장 | 수강 종료 | PATCH | `/enrollments/{enrollmentId}/end-date` | FR-2.7 |
| API-8 | 원장 | 고지서 생성 작업 실행 | POST | `/billing-jobs` | FR-3.7 · FR-3.10 |
| API-9 | 원장 | 고지서 항목 금액 수정 | PATCH | `/invoices/{invoiceId}/items/{itemId}/amount` | FR-3.5 · FR-3.14 · FR-3.15 |
| API-10 | 공급사 | 발송 결과 웹훅 수신 | POST | `/webhooks/kakao` | FR-5.1~5.5 |
| API-11 | 원장 | 납부 처리 | POST | `/invoices/{invoiceId}/payments` | FR-6.1~6.7 |
| API-12 | 원장 | 미납 목록 조회 | GET | `/invoices/overdue` | FR-6.8 |
| API-13 | 원장 | 발송 진행 현황 조회 | GET | `/billing-jobs/{billingMonth}` | FR-7.1 |
| API-14 | 원장 | 발송 실패 건 조회 | GET | `/billing-jobs/{billingMonth}/failures` | FR-7.2 |
| API-15 | 원장 | 고지서 단건 이력 조회 | GET | `/invoices/{invoiceId}/history` | FR-7.3 |
| API-16 | 원장 | 실패 건 재발송 | POST | `/invoices/{invoiceId}/resend` | FR-7.4 · FR-7.5 |

---

## 3. API 상세

### API-1. 학원 등록 · `POST /academies`

- 액터: 운영자 (헤더 불요 — 학원 스코프 밖)
- 관련: FR-2.12 / TC-2-20
- 플랜 · 할인 설정은 생성 시에만 지정한다 — 변경 API는 범위 밖 (D-48)

**Request**

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `name` | String | O | `@NotBlank`, 최대 50자 | 학원명 |
| `plan` | String | O | `@NotNull`, `FREE`/`PAID` | 플랜 (FR-5.9) |
| `siblingDiscountRate` | Integer | O | `@Min(0) @Max(100)` | 형제 할인율 % (D-5) |
| `siblingDiscountTarget` | String | O | `@NotNull`, `FROM_SECOND`/`ALL` | 할인 대상 (D-5) |

```json
{ "name": "강남수학학원", "plan": "PAID", "siblingDiscountRate": 10, "siblingDiscountTarget": "FROM_SECOND" }
```

**Response** · `201 Created`

| 필드 | 타입 | 설명 |
|---|---|---|
| `academyId` | Long | 생성된 학원 ID |

```json
{ "academyId": 3 }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 필드 검증 실패 |

---

### API-2. 학부모 등록 (수강생 포함) · `POST /parents`

- 액터: 원장
- 관련: FR-2.10 · FR-2.12 / TC-2-21

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `name` | String | O | `@NotBlank`, 최대 50자 | 학부모 이름 |
| `phone` | String | O | `@NotBlank`, `010-\d{4}-\d{4}` | 전화번호 (D-6) |
| `students` | List | O | `@NotEmpty @Valid` | 수강생 목록 |
| `students[].name` | String | O | `@NotBlank`, 최대 50자 | 수강생 이름 |

```json
{ "name": "김학부모", "phone": "010-1000-0001", "students": [{ "name": "김첫째" }, { "name": "김둘째" }] }
```

**Response** · `201 Created`

| 필드 | 타입 | 설명 |
|---|---|---|
| `parentId` | Long | 학부모 ID |
| `studentIds` | List\<Long\> | 수강생 ID 목록 (요청 순서) |

```json
{ "parentId": 4, "studentIds": [5, 6] }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 필드 검증 실패 · 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |

---

### API-3. 수신 거부 변경 · `PATCH /parents/{parentId}/notification-refusal`

- 액터: 원장
- 관련: FR-2.11 / TC-2-19 · TC-2-22

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `refused` | Boolean | O | `@NotNull` | true = 수신 거부 |

```json
{ "refused": true }
```

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `parentId` | Long | 학부모 ID |
| `notificationRefused` | Boolean | 변경 후 값 |

```json
{ "parentId": 1, "notificationRefused": true }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 필드 검증 실패 · 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `PARENT_NOT_FOUND` | 없거나 타 학원 학부모 |

---

### API-4. 강좌 등록 · `POST /courses`

- 액터: 원장
- 관련: FR-2.4 · FR-2.5 / TC-2-01~03 · TC-2-05~07

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `name` | String | O | `@NotBlank`, 최대 50자 | 강좌명 |
| `monthlyFee` | Long | O | `@NotNull @PositiveOrZero` | 월 원비 (원) |
| `classDays` | List\<String\> | O | `@NotEmpty`, 요소 중복 불가, `MON`~`SUN` | 수업 요일 (복수) |
| `capacity` | Integer | O | `@NotNull @Min(1)` | 정원 |

```json
{ "name": "중등수학A", "monthlyFee": 300000, "classDays": ["MON", "WED", "FRI"], "capacity": 10 }
```

**Response** · `201 Created`

| 필드 | 타입 | 설명 |
|---|---|---|
| `courseId` | Long | 강좌 ID |

```json
{ "courseId": 4 }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 빈 · 중복 요일, 음수 원비, 정원 < 1, 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |

---

### API-5. 강좌 조회 · `GET /courses/{courseId}`

- 액터: 원장
- 관련: FR-2.1 · FR-2.2 / TC-2-04

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `courseId` | Long | 강좌 ID |
| `name` | String | 강좌명 |
| `monthlyFee` | Long | 월 원비 |
| `classDays` | List\<String\> | 수업 요일 |
| `capacity` | Integer | 정원 |
| `enrolledCount` | Integer | 현재 인원 (D-41) |

```json
{ "courseId": 1, "name": "중등수학A", "monthlyFee": 300000, "classDays": ["MON", "WED", "FRI"], "capacity": 10, "enrolledCount": 2 }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `COURSE_NOT_FOUND` | 없거나 타 학원 강좌 (존재 비노출) |

---

### API-6. 수강 등록 · `POST /enrollments`

- 액터: 원장
- 관련: FR-2.6 · FR-2.8 · FR-2.9 / TC-2-08~16 · TC-2-18 · TC-2-23

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `courseId` | Long | O | `@NotNull` | 강좌 ID |
| `studentId` | Long | O | `@NotNull` | 수강생 ID |
| `startDate` | String | O | `@NotNull`, `yyyy-MM-dd` | 시작일 |
| `endDate` | String | X | `yyyy-MM-dd`, 시작일 이후 | 종료일 (없으면 무기한) |
| `paymentDay` | Integer | O | `@NotNull @Min(1) @Max(31)` | 납부일 |

```json
{ "courseId": 1, "studentId": 1, "startDate": "2026-03-11", "endDate": null, "paymentDay": 10 }
```

**Response** · `201 Created`

| 필드 | 타입 | 설명 |
|---|---|---|
| `enrollmentId` | Long | 수강 등록 ID |

```json
{ "enrollmentId": 10 }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 납부일 범위 밖 · 종료일 < 시작일 · 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `COURSE_NOT_FOUND` | 없거나 타 학원 강좌 |
| 404 | `STUDENT_NOT_FOUND` | 없거나 타 학원 수강생 |
| 409 | `COURSE_CAPACITY_EXCEEDED` | 정원 초과 (동시 요청 포함, FR-2.8) |
| 409 | `ENROLLMENT_PERIOD_OVERLAPPED` | 같은 수강생 · 강좌 기간 중복 (D-43) |

---

### API-7. 수강 종료 · `PATCH /enrollments/{enrollmentId}/end-date`

- 액터: 원장
- 관련: FR-2.7 · FR-3.13 / TC-2-17 · TC-2-24 · TC-2-25 · TC-3-16 · TC-3-17 · TC-3-20

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `endDate` | String | O | `@NotNull`, `yyyy-MM-dd` | 종료일 (덮어쓰기 허용) |

```json
{ "endDate": "2026-03-20" }
```

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `enrollmentId` | Long | 수강 등록 ID |
| `endDate` | String | 저장된 종료일 |

```json
{ "enrollmentId": 10, "endDate": "2026-03-20" }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 날짜 형식 오류 · 헤더 누락 |
| 400 | `INVALID_ENROLLMENT_PERIOD` | 종료일 < 시작일 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `ENROLLMENT_NOT_FOUND` | 없거나 타 학원 등록 |

---

### API-8. 고지서 생성 작업 실행 · `POST /billing-jobs`

- 액터: 원장
- 관련: FR-3.7 · FR-3.8 · FR-3.10 · FR-3.17 / TC-3-08~10 · TC-3-12 · TC-3-18 · TC-3-19
- 작업 단위는 (월, 학원) (D-13) — 전 학원 일괄은 월초 스케줄러 경로 (FR-3.9)
- 재실행은 없는 건만 증분 생성한다 (D-38)

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `billingMonth` | String | O | `@NotNull`, `yyyy-MM` | 고지 월 |

```json
{ "billingMonth": "2026-03" }
```

**Response** · `201 Created` (신규 작업) / `200 OK` (재실행 — 증분)

| 필드 | 타입 | 설명 |
|---|---|---|
| `jobId` | Long | 작업 ID |
| `billingMonth` | String | 고지 월 |
| `status` | String | `COMPLETED` 등 |
| `createdCount` | Integer | 이번 실행 생성 건수 (0건 가능, FR-3.17) |

```json
{ "jobId": 1, "billingMonth": "2026-03", "status": "COMPLETED", "createdCount": 42 }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 월 형식 오류 · 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |

---

### API-9. 고지서 항목 금액 수정 · `PATCH /invoices/{invoiceId}/items/{itemId}/amount`

- 액터: 원장
- 관련: FR-3.5 · FR-3.14 · FR-3.15 / TC-3-13~15 · TC-3-24
- 수정해도 재발송하지 않는다 (D-26)

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `amount` | Long | O | `@NotNull @PositiveOrZero` | 수정 금액 (0원 허용, D-39) |

```json
{ "amount": 150000 }
```

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `invoiceId` | Long | 고지서 ID |
| `itemId` | Long | 항목 ID |
| `amount` | Long | 수정 후 항목 금액 |
| `totalAmount` | Long | 재계산된 고지서 총액 |
| `paymentStatus` | String | 재판정된 납부 상태 |

```json
{ "invoiceId": 1, "itemId": 2, "amount": 150000, "totalAmount": 336922, "paymentStatus": "PARTIAL" }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 음수 금액 · 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `INVOICE_NOT_FOUND` | 없거나 타 학원 고지서 (D-25) |
| 404 | `INVOICE_ITEM_NOT_FOUND` | 고지서에 없는 항목 |
| 409 | `INVOICE_MODIFY_BELOW_PAID` | 납부액 미만으로 수정 (D-26) |

---

### API-10. 발송 결과 웹훅 수신 · `POST /webhooks/kakao`

- 액터: 공급사 (가짜 클라이언트) — 학원 헤더 불요 (FR-5.1)
- 관련: FR-5.1~5.5 · FR-5.7 · FR-5.8 / TC-5-01~03 · TC-5-05~10
- 같은 messageId는 한 번만 반영 (FR-5.4) · 미매칭은 기록만 하고 200 (TC-5-10)

**Request**

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `messageId` | String | O | `@NotBlank`, UUID 36자 | 발송 시 발급한 messageId |
| `resultCode` | String | O | `@NotBlank`, 세 코드만 | `DELIVERED` / `FAILED_NOT_KAKAO_USER` / `FAILED_ETC` |

```json
{ "messageId": "550e8400-e29b-41d4-a716-446655440000", "resultCode": "DELIVERED" }
```

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `processed` | Boolean | true = 반영, false = 미매칭 · 중복 (기록만) |

```json
{ "processed": true }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 필드 누락 · 형식 오류 |
| 400 | `INVALID_RESULT_CODE` | 세 코드 외의 값 (FR-5.3, 상태 변경 없음) |

---

### API-11. 납부 처리 · `POST /invoices/{invoiceId}/payments`

- 액터: 원장
- 관련: FR-6.1~6.7 / TC-6-01~08 · TC-6-10 · TC-6-12
- 발송 상태와 무관하게 납부 가능 (EXCLUDED · 미확정 포함, D-37)

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |
| `Idempotency-Key` | O | UUID — 누락 시 400 (D-21), 키 범위는 학원 (D-22) |

Body

| 필드 | 타입 | 필수 | 검증 | 설명 |
|---|---|---|---|---|
| `amount` | Long | O | `@NotNull @Positive` | 납부 금액 (0 이하 400) |

```json
{ "amount": 100000 }
```

**Response** · `200 OK` (멱등 재요청도 첫 응답 그대로)

| 필드 | 타입 | 설명 |
|---|---|---|
| `invoiceId` | Long | 고지서 ID |
| `totalAmount` | Long | 고지 금액 |
| `paidAmount` | Long | 납부 합산액 |
| `paymentStatus` | String | `UNPAID` / `PARTIAL` / `PAID` |

```json
{ "invoiceId": 1, "totalAmount": 300000, "paidAmount": 100000, "paymentStatus": "PARTIAL" }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 금액 0 이하 · 학원 헤더 누락 |
| 400 | `IDEMPOTENCY_KEY_REQUIRED` | `Idempotency-Key` 누락 (FR-6.4) |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `INVOICE_NOT_FOUND` | 없거나 타 학원 고지서 |
| 409 | `PAYMENT_EXCEEDS_BALANCE` | 잔액 초과 (FR-6.6) |
| 422 | `IDEMPOTENCY_KEY_CONFLICT` | 같은 키 · 다른 본문 (FR-6.5) |

---

### API-12. 미납 목록 조회 · `GET /invoices/overdue`

- 액터: 원장
- 관련: FR-6.8 / TC-6-09 · TC-6-11
- 미납 = `UNPAID` · `PARTIAL`, 연체일수 1 이상만 (기한 당일 제외, D-52)
- 미납 판정은 현재 상태 기준 (D-29)

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Query

| 이름 | 필수 | 검증 | 설명 |
|---|---|---|---|
| `baseDate` | X | `yyyy-MM-dd` | 기준일 (기본 = 오늘) |
| `offset` | X | `@Min(0)` | 기본 0 (D-30) |
| `limit` | X | `@Min(1) @Max(100)` | 기본 20 |

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `totalCount` | Long | 조건 일치 전체 건수 |
| `items` | List | 연체일수 내림차순 |
| `items[].invoiceId` | Long | 고지서 ID |
| `items[].parentId` | Long | 학부모 ID |
| `items[].billingMonth` | String | 고지 월 |
| `items[].totalAmount` | Long | 고지 금액 |
| `items[].paidAmount` | Long | 납부 합산액 |
| `items[].dueDate` | String | 납부기한 |
| `items[].overdueDays` | Integer | 연체일수 (≥ 1) |
| `items[].paymentStatus` | String | `UNPAID` / `PARTIAL` |

```json
{
  "totalCount": 2,
  "items": [
    { "invoiceId": 1, "parentId": 1, "billingMonth": "2026-03", "totalAmount": 300000, "paidAmount": 0, "dueDate": "2026-03-10", "overdueDays": 12, "paymentStatus": "UNPAID" }
  ]
}
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 날짜 · 페이지 파라미터 오류, 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |

---

### API-13. 발송 진행 현황 조회 · `GET /billing-jobs/{billingMonth}`

- 액터: 원장
- 관련: FR-7.1 / TC-7-01 · TC-7-08
- 상태별 건수는 실시간 GROUP BY (D-14), 초당 처리량 = 최근 10초 send 성공 ÷ 10 (D-15)

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `billingMonth` | String | 고지 월 |
| `jobStatus` | String | 작업 상태 |
| `totalCount` | Long | 고지서 전체 건수 |
| `counts` | Object | 발송 상태별 건수 (8개 상태 전부, 0 포함) |
| `throughputPerSecond` | Double | 초당 처리량 |

```json
{
  "billingMonth": "2026-03",
  "jobStatus": "COMPLETED",
  "totalCount": 50000,
  "counts": { "READY": 1000, "SENDING": 50, "REQUESTED": 200, "SENT": 48000, "FAILED": 300, "UNCONFIRMED": 150, "EXCLUDED": 250, "CANCELED": 50 },
  "throughputPerSecond": 98.5
}
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 월 형식 오류 · 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `BILLING_JOB_NOT_FOUND` | 해당 (월, 학원) 작업 없음 |

---

### API-14. 발송 실패 건 조회 · `GET /billing-jobs/{billingMonth}/failures`

- 액터: 원장
- 관련: FR-7.2 / TC-7-02

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Query

| 이름 | 필수 | 검증 | 설명 |
|---|---|---|---|
| `reason` | X | `RATE_LIMITED`/`TIMEOUT`/`FAILED_NOT_KAKAO_USER`/`FAILED_ETC`/`UNCONFIRMED` | 사유 필터 (없으면 전체) |
| `offset` | X | `@Min(0)` | 기본 0 |
| `limit` | X | `@Min(1) @Max(100)` | 기본 20 |

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `totalCount` | Long | 조건 일치 전체 건수 |
| `items[].invoiceId` | Long | 고지서 ID |
| `items[].parentId` | Long | 학부모 ID |
| `items[].sendStatus` | String | `FAILED` / `UNCONFIRMED` |
| `items[].failReason` | String | 실패 사유 |
| `items[].lastAttemptAt` | String | 마지막 발송 시도 시각 |

```json
{
  "totalCount": 1,
  "items": [
    { "invoiceId": 7, "parentId": 2, "sendStatus": "FAILED", "failReason": "RATE_LIMITED", "lastAttemptAt": "2026-03-01T09:12:31" }
  ]
}
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 사유 · 페이지 파라미터 오류, 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `BILLING_JOB_NOT_FOUND` | 해당 (월, 학원) 작업 없음 |

---

### API-15. 고지서 단건 이력 조회 · `GET /invoices/{invoiceId}/history`

- 액터: 원장
- 관련: FR-7.3 / TC-7-03
- 생성 · 금액 수정 · 발송 시도 · 웹훅 확정 · 미확정 · SMS 대체 · 재발송 · 납부 · 미납 안내를 시간순으로 조합

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

**Response** · `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| `invoiceId` | Long | 고지서 ID |
| `sendStatus` | String | 현재 발송 상태 |
| `paymentStatus` | String | 현재 납부 상태 |
| `events` | List | 시간 오름차순 |
| `events[].type` | String | `CREATED`/`AMOUNT_ADJUSTED`/`SEND_REQUESTED`/`WEBHOOK_CONFIRMED`/`UNCONFIRMED`/`SMS_FALLBACK`/`RESEND`/`PAYMENT`/`OVERDUE_NOTICE` |
| `events[].occurredAt` | String | 발생 시각 |
| `events[].detail` | Object | messageId · 채널 · 금액 · 결과 코드 등. `AMOUNT_ADJUSTED`는 `reason`(`MANUAL` / `RECALC_FLOORED`) 포함 (D-53) |

```json
{
  "invoiceId": 1,
  "sendStatus": "SENT",
  "paymentStatus": "PAID",
  "events": [
    { "type": "CREATED", "occurredAt": "2026-03-01T09:00:01", "detail": { "totalAmount": 300000 } },
    { "type": "SEND_REQUESTED", "occurredAt": "2026-03-01T09:10:00", "detail": { "messageId": "550e...", "channel": "KAKAO" } },
    { "type": "WEBHOOK_CONFIRMED", "occurredAt": "2026-03-01T09:10:02", "detail": { "resultCode": "DELIVERED" } },
    { "type": "PAYMENT", "occurredAt": "2026-03-05T14:00:00", "detail": { "amount": 300000 } }
  ]
}
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `INVOICE_NOT_FOUND` | 없거나 타 학원 고지서 |

---

### API-16. 실패 건 재발송 · `POST /invoices/{invoiceId}/resend`

- 액터: 원장
- 관련: FR-7.4 · FR-7.5 / TC-7-04~07
- `FAILED` · `UNCONFIRMED`만 접수 → `READY` 복귀 → 발송 워커가 새 messageId로 발송
- 금지 시간에도 접수는 되고 send는 08:00 이후 (D-9)

**Request**

Header

| 이름 | 필수 | 설명 |
|---|---|---|
| `X-Academy-Id` | O | 학원 ID |

Body 없음

**Response** · `202 Accepted`

| 필드 | 타입 | 설명 |
|---|---|---|
| `invoiceId` | Long | 고지서 ID |
| `sendStatus` | String | `READY` (재발송 대기) |

```json
{ "invoiceId": 7, "sendStatus": "READY" }
```

**Errors**

| 상태 | errorCode | 조건 |
|---|---|---|
| 400 | `INVALID_INPUT` | 헤더 누락 |
| 404 | `ACADEMY_NOT_FOUND` | 존재하지 않는 학원 |
| 404 | `INVOICE_NOT_FOUND` | 없거나 타 학원 고지서 |
| 409 | `RESEND_NOT_ALLOWED` | `SENT` 등 재발송 불가 상태 (TC-7-05) |

---

## 4. 에러 코드

### 공통

| errorCode | HTTP | 메시지 | 발생 조건 |
|---|---|---|---|
| `INVALID_INPUT` | 400 | 입력값이 올바르지 않습니다. | 입력 형식 오류 (아래) |
| `RESOURCE_NOT_FOUND` | 404 | 요청한 리소스를 찾을 수 없습니다. | 매핑되지 않은 경로 |
| `METHOD_NOT_ALLOWED` | 405 | 지원하지 않는 HTTP 메서드입니다. | 경로는 있으나 메서드가 다름 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 오류가 발생했습니다. | 처리되지 않은 예외 |

`INVALID_INPUT`

- 메시지
  - 본문 검증 실패 시 `필드: 사유`
- 발생 조건
  - Bean Validation 실패
  - `X-Academy-Id` 헤더 누락 · 형식 오류(비숫자 포함, TC-2-26) (FR-2.3)
  - 파라미터 형식 오류
  - 본문 파싱 실패

`RESOURCE_NOT_FOUND` · `METHOD_NOT_ALLOWED` · `INTERNAL_SERVER_ERROR`는 프레임워크 공통 처리(전역 핸들러 기본 동작)라 개별 TC를 두지 않는다

### 학원 · 학부모

| errorCode | HTTP | 메시지 | 발생 조건 | 예외 클래스 | API |
|---|---|---|---|---|---|
| `ACADEMY_NOT_FOUND` | 404 | 학원을 찾을 수 없습니다. | 헤더의 학원 미존재 (D-32) | `AcademyNotFoundException` | 헤더 필요한 전부 |
| `PARENT_NOT_FOUND` | 404 | 학부모를 찾을 수 없습니다. | 없거나 타 학원 학부모 | `ParentNotFoundException` | API-3 |
| `STUDENT_NOT_FOUND` | 404 | 수강생을 찾을 수 없습니다. | 없거나 타 학원 수강생 | `StudentNotFoundException` | API-6 |

### 강좌 · 수강

| errorCode | HTTP | 메시지 | 발생 조건 | 예외 클래스 | API |
|---|---|---|---|---|---|
| `COURSE_NOT_FOUND` | 404 | 강좌를 찾을 수 없습니다. | 없거나 타 학원 강좌 (FR-2.2) | `CourseNotFoundException` | API-5 · API-6 |
| `COURSE_CAPACITY_EXCEEDED` | 409 | 정원을 초과했습니다. | 현재 인원 ≥ 정원 (FR-2.8) | `CourseCapacityExceededException` | API-6 |
| `ENROLLMENT_NOT_FOUND` | 404 | 수강 등록을 찾을 수 없습니다. | 없거나 타 학원 등록 | `EnrollmentNotFoundException` | API-7 |
| `ENROLLMENT_PERIOD_OVERLAPPED` | 409 | 수강 기간이 중복됩니다. | 같은 수강생 · 강좌 기간 겹침 (FR-2.9) | `EnrollmentPeriodOverlappedException` | API-6 |
| `INVALID_ENROLLMENT_PERIOD` | 400 | 수강 기간이 올바르지 않습니다. | 종료일 < 시작일 (FR-2.7) | `InvalidEnrollmentPeriodException` | API-7 |

### 고지서 · 납부

| errorCode | HTTP | 메시지 | 발생 조건 | 예외 클래스 | API |
|---|---|---|---|---|---|
| `INVOICE_NOT_FOUND` | 404 | 고지서를 찾을 수 없습니다. | 없거나 타 학원 고지서 (D-25) | `InvoiceNotFoundException` | API-9 · API-11 · API-15 · API-16 |
| `INVOICE_ITEM_NOT_FOUND` | 404 | 고지서 항목을 찾을 수 없습니다. | 고지서에 없는 항목 (FR-3.5) | `InvoiceItemNotFoundException` | API-9 |
| `INVOICE_MODIFY_BELOW_PAID` | 409 | 납부액 미만으로 수정할 수 없습니다. | 수정액 < 납부 합산 (FR-3.15) | `InvoiceModifyBelowPaidException` | API-9 |
| `PAYMENT_EXCEEDS_BALANCE` | 409 | 잔액을 초과하는 납부입니다. | 납부액 > 잔액 (FR-6.6) | `PaymentExceedsBalanceException` | API-11 |
| `IDEMPOTENCY_KEY_REQUIRED` | 400 | Idempotency-Key 헤더가 필요합니다. | 헤더 누락 (FR-6.4) | `IdempotencyKeyRequiredException` | API-11 |
| `IDEMPOTENCY_KEY_CONFLICT` | 422 | 같은 키로 다른 요청이 이미 처리됐습니다. | 같은 키 · 다른 본문 (FR-6.5) | `IdempotencyKeyConflictException` | API-11 |

### 작업 · 발송

| errorCode | HTTP | 메시지 | 발생 조건 | 예외 클래스 | API |
|---|---|---|---|---|---|
| `BILLING_JOB_NOT_FOUND` | 404 | 발송 작업을 찾을 수 없습니다. | 해당 (월, 학원) 작업 없음 (D-13) | `BillingJobNotFoundException` | API-13 · API-14 |
| `INVALID_RESULT_CODE` | 400 | 알 수 없는 결과 코드입니다. | 세 코드 외 웹훅 (FR-5.3) | `InvalidResultCodeException` | API-10 |
| `RESEND_NOT_ALLOWED` | 409 | 재발송할 수 없는 상태입니다. | `SENT` 등 (FR-7.4) | `ResendNotAllowedException` | API-16 |
| `INVALID_STATUS_TRANSITION` | 409 | 허용되지 않는 상태 전이입니다. | 02 §4 표 밖의 전이 | `InvalidStatusTransitionException` | 내부 방어 |
