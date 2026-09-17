package com.academy.tuition.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 납부일 VO. 1~31 (02 §1).
 * 결손 월의 말일 당김(D-7)은 F3의 DueDatePolicy가 맡는다.
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDay {

    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 31;

    @Column(nullable = false)
    private int value;

    private PaymentDay(int value) {
        this.value = value;
    }

    public static PaymentDay of(int value) {
        if (value < MIN_DAY || value > MAX_DAY) {
            throw new IllegalArgumentException("납부일은 1~31이어야 합니다: " + value);
        }
        return new PaymentDay(value);
    }
}
