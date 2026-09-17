# F1. 기초 설정 `feature/setup`

## 받은 지시

- `/run-feature F1` — task_list F1 블록 실행
  - T1-1 `feat: 공통 값 객체 및 enum 구현` — 02 §1
  - T1-2 `feat: 도메인 에러 코드 추가` — 03 §4
  - T1-3 `feat: 기초 데이터 엔티티 및 seed 구현` — 02 §9 · TC-1-09
  - T1-4 `test: 값 객체 성공·실패·엣지 케이스` — TC-1-01 ~ TC-1-08

## 작업 흐름

1. T1-1 (5701f94)
   - 값 객체 5종 구현
     - Money · PaymentDay · BillingMonth · ClassDays(+Converter) · EnrollmentPeriod
   - enum 2종 구현 (AcademyPlan · SiblingDiscountTarget)
2. T1-2 (764650d)
   - ErrorCode 19개 추가
   - 03 §4와 1:1 — 코드 · HTTP 상태 · 메시지
3. T1-3 (f62f43c)
   - 엔티티 4종 구현 (Academy · Parent · Student · Course)
     - 정적 팩토리 + 불변식
   - `data.sql` 작성 — 02 §9 행 단위 일치 · `INSERT IGNORE` · `NOW(6)`
   - 02 §9 대조 완료 체크
4. T1-4 (ebddd2a)
   - 값 객체 테스트 TC-1-01 ~ TC-1-08
   - SeedDataTest TC-1-09
5. `reviewer` + `verifier` 병렬 점검 → 지적 정리 → 미수정 유지 결정

## 바뀐 파일

- `src/main/java/com/academy/tuition/domain/common/Money.java` (생성) — 금액 VO
- `src/main/java/com/academy/tuition/domain/common/PaymentDay.java` (생성) — 납부일 VO
- `src/main/java/com/academy/tuition/domain/common/BillingMonth.java` (생성) — 청구 월 VO
- `src/main/java/com/academy/tuition/domain/course/ClassDays.java` (생성) — 수업 요일 VO
- `src/main/java/com/academy/tuition/domain/course/ClassDaysConverter.java` (생성) — ClassDays JPA 컨버터
- `src/main/java/com/academy/tuition/domain/enrollment/EnrollmentPeriod.java` (생성) — 수강 기간 VO
- `src/main/java/com/academy/tuition/domain/academy/AcademyPlan.java` (생성) — 플랜 enum
- `src/main/java/com/academy/tuition/domain/academy/SiblingDiscountTarget.java` (생성) — 형제 할인 대상 enum
- `src/main/java/com/academy/tuition/domain/exception/ErrorCode.java` (수정) — 도메인 에러 코드 19개 추가 (총 23개)
- `src/main/java/com/academy/tuition/domain/academy/exception/AcademyException.java` (생성, T1-2) — 학원 도메인 예외
- `src/main/java/com/academy/tuition/domain/parent/exception/ParentException.java` (생성, T1-2) — 학부모 도메인 예외
- `src/main/java/com/academy/tuition/domain/course/exception/CourseException.java` (생성, T1-2) — 강좌 도메인 예외
- `src/main/java/com/academy/tuition/domain/academy/Academy.java` (생성) — 학원 엔티티
- `src/main/java/com/academy/tuition/domain/parent/Parent.java` (생성) — 학부모 엔티티
- `src/main/java/com/academy/tuition/domain/parent/Student.java` (생성) — 학생 엔티티
- `src/main/java/com/academy/tuition/domain/course/Course.java` (생성) — 강좌 엔티티
- `src/main/resources/data.sql` (생성) — seed 데이터 (02 §9)
- `src/test/java/com/academy/tuition/domain/common/MoneyTest.java` (생성) — TC-1-01 · TC-1-02
- `src/test/java/com/academy/tuition/domain/common/PaymentDayTest.java` (생성) — TC-1-03 · TC-1-04
- `src/test/java/com/academy/tuition/domain/common/BillingMonthTest.java` (생성) — BillingMonth 엣지
- `src/test/java/com/academy/tuition/domain/course/ClassDaysTest.java` (생성) — TC-1-05 · TC-1-06
- `src/test/java/com/academy/tuition/domain/enrollment/EnrollmentPeriodTest.java` (생성) — TC-1-07 · TC-1-08
- `src/test/java/com/academy/tuition/application/SeedDataTest.java` (생성) — TC-1-09

## 설계대로 한 것

- 값 객체 5종의 제약을 02 §1 그대로 구현
- ErrorCode를 03 §4와 1:1로 추가 — 코드 · 상태 · 메시지 일치
- 엔티티 nullable 선언을 02 §6 ERD와 일치
- `data.sql`을 02 §9와 행 단위로 일치시키고 대조 체크 완료
- TC-1-01 ~ TC-1-09 전부 구현 · `@DisplayName` 부여

## 설계에 없어서 정한 것

- enum 범위 / 전체 vs F1 필요분 / F1에서 쓰는 2종만 먼저 구현
- ClassDays 직렬화 / 표기 방식 자유 / 3글자 토큰(MON 등) · 자연 정렬로 고정
- ClassDays 중복 입력 / BusinessException vs IAE / IAE (VO는 최후 방어선, 4xx 변환은 도메인 몫)
- Parent→Student 연관 / 양방향 vs 단방향 / 단방향 `@OneToMany` + `@OrderBy`
- 엔티티 행위 메서드 시점 / 미리 vs 필요 시 / 사용하는 기능(F)에서 추가

## 설계와 다르게 간 것

- parent · course의 `academy_id`가 식별자 참조라 FK · 인덱스 미생성
  - 02 §6 전제와 어긋남
  - F2 격리 조회 성능에 영향 가능
  - 리뷰 중간 지적 — 코드 높음 없음이라 미수정, PR 본문으로 이관
- `Parent.create`가 빈 students를 허용
  - API-2 `@NotEmpty`와 방어 깊이 불일치
  - 같은 이유로 미수정, F2에서 다룰 후보

## 검증

- `./gradlew spotlessApply build` → 작업(T)마다 4회, 전부 1회 통과
  - 테스트 45개 (신규 25)
- `.http` 실측 → 대상 없음 (F1은 API 없음)
- `reviewer` → 높음 0 · 중간 3 · 낮음 3, 전부 미수정 유지
  - 중간: academy_id FK · 인덱스 미생성 / Parent.create 빈 students 허용 / 엔티티 팩토리 검증 테스트 부재
  - 낮음: TC ID 없는 엣지 테스트 9건 / 03 §4 예외 클래스 컬럼과 코드 규칙 불일치 / BillingMonth `@Column`이 `@AttributeOverride`로 대체될 위험
- `verifier` 4방향 점검 → 문서 대비 불일치 없음
  - VO 5/5 · ErrorCode 23/23 · seed 4테이블 존재 확인
  - ErrorCode 코드 · 상태 · 메시지, data.sql 전 값, VO 제약, nullable 선언 전부 문서와 일치
  - 지적은 리뷰와 동근원 (03 §4 예외 클래스 컬럼 · academy_id 인덱스 · 빈 students · SeedDataTest 상한 미검증 · TC ID 없는 9건)

## 에러와 해결

- 없음 (게이트 4회 전부 1회 통과)

## 자주 틀리는 것 후보

- 없음
