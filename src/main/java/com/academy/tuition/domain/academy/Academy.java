package com.academy.tuition.domain.academy;

import com.academy.tuition.domain.academy.exception.AcademyException;
import com.academy.tuition.domain.common.BaseTimeEntity;
import com.academy.tuition.domain.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학원 애그리거트 (02 §3.1). 요청 헤더 X-Academy-Id로 식별한다 (FR-2.1).
 */
@Getter
@Entity
@Table(name = "academy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Academy extends BaseTimeEntity {

    private static final int MIN_DISCOUNT_RATE = 0;
    private static final int MAX_DISCOUNT_RATE = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademyPlan plan;

    @Column(nullable = false)
    private int siblingDiscountRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SiblingDiscountTarget siblingDiscountTarget;

    private Academy(
            String name, AcademyPlan plan, int siblingDiscountRate, SiblingDiscountTarget siblingDiscountTarget) {
        this.name = name;
        this.plan = plan;
        this.siblingDiscountRate = siblingDiscountRate;
        this.siblingDiscountTarget = siblingDiscountTarget;
    }

    public static Academy create(
            String name, AcademyPlan plan, int siblingDiscountRate, SiblingDiscountTarget siblingDiscountTarget) {
        if (name == null || name.isBlank()) {
            throw new AcademyException(ErrorCode.INVALID_INPUT, "name: 학원명은 필수입니다");
        }
        if (plan == null) {
            throw new AcademyException(ErrorCode.INVALID_INPUT, "plan: 플랜은 필수입니다");
        }
        if (siblingDiscountRate < MIN_DISCOUNT_RATE || siblingDiscountRate > MAX_DISCOUNT_RATE) {
            throw new AcademyException(ErrorCode.INVALID_INPUT, "siblingDiscountRate: 할인율은 0~100이어야 합니다");
        }
        if (siblingDiscountTarget == null) {
            throw new AcademyException(ErrorCode.INVALID_INPUT, "siblingDiscountTarget: 할인 대상은 필수입니다");
        }
        return new Academy(name, plan, siblingDiscountRate, siblingDiscountTarget);
    }
}
