package com.academy.tuition.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 금액 VO. 원 단위 정수로 보관한다 (02 §1).
 * 음수 금액은 만들 수 없다. 사용자 입력 오류는 이 생성 전에 BusinessException으로 막는다.
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    @Column(nullable = false)
    private long amount;

    private Money(long amount) {
        this.amount = amount;
    }

    public static Money of(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + amount);
        }
        return new Money(amount);
    }
}
