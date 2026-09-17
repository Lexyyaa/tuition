package com.academy.tuition.domain.enrollment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수강 기간 VO. 시작일 · 종료일(null = 무기한) (02 §1).
 * 종료일이 있으면 종료일 ≥ 시작일 (02 §3.4 불변식).
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnrollmentPeriod {

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    private EnrollmentPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static EnrollmentPeriod of(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("시작일은 필수입니다");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다: 시작일 " + startDate + ", 종료일 " + endDate);
        }
        return new EnrollmentPeriod(startDate, endDate);
    }

    public boolean isIndefinite() {
        return endDate == null;
    }
}
