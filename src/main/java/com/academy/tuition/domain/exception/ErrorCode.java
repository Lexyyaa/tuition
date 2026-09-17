package com.academy.tuition.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 단일 관리. docs/design/03-api-spec.md §4와 1:1로 맞춘다.
 * 새 코드는 문서에 먼저 추가하고, 도메인별 주석 블록 아래에 모은다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 학원 · 학부모
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "학원을 찾을 수 없습니다."),
    PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "학부모를 찾을 수 없습니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수강생을 찾을 수 없습니다."),

    // 강좌 · 수강
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "강좌를 찾을 수 없습니다."),
    COURSE_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "정원을 초과했습니다."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 등록을 찾을 수 없습니다."),
    ENROLLMENT_PERIOD_OVERLAPPED(HttpStatus.CONFLICT, "수강 기간이 중복됩니다."),
    INVALID_ENROLLMENT_PERIOD(HttpStatus.BAD_REQUEST, "수강 기간이 올바르지 않습니다."),

    // 고지서 · 납부
    INVOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "고지서를 찾을 수 없습니다."),
    INVOICE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "고지서 항목을 찾을 수 없습니다."),
    INVOICE_MODIFY_BELOW_PAID(HttpStatus.CONFLICT, "납부액 미만으로 수정할 수 없습니다."),
    PAYMENT_EXCEEDS_BALANCE(HttpStatus.CONFLICT, "잔액을 초과하는 납부입니다."),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "Idempotency-Key 헤더가 필요합니다."),
    IDEMPOTENCY_KEY_CONFLICT(HttpStatus.UNPROCESSABLE_ENTITY, "같은 키로 다른 요청이 이미 처리됐습니다."),

    // 작업 · 발송
    BILLING_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "발송 작업을 찾을 수 없습니다."),
    INVALID_RESULT_CODE(HttpStatus.BAD_REQUEST, "알 수 없는 결과 코드입니다."),
    RESEND_NOT_ALLOWED(HttpStatus.CONFLICT, "재발송할 수 없는 상태입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "허용되지 않는 상태 전이입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
