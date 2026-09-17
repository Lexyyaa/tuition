package com.academy.tuition.domain.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnrollmentPeriodTest {

    @Test
    @DisplayName("[TC-1-07] 수강 기간 VO를 시작일만으로 생성하면 무기한 기간이 된다 (종료일 null)")
    void createIndefinitePeriod() {
        // given
        LocalDate startDate = LocalDate.of(2026, 3, 1);

        // when
        EnrollmentPeriod period = EnrollmentPeriod.of(startDate, null);

        // then
        assertThat(period.getStartDate()).isEqualTo(startDate);
        assertThat(period.getEndDate()).isNull();
        assertThat(period.isIndefinite()).isTrue();
    }

    @Test
    @DisplayName("[TC-1-08] 수강 기간 VO의 종료일이 시작일보다 앞서면 IllegalArgumentException이 발생한다")
    void createInvalidPeriod() {
        // given
        LocalDate startDate = LocalDate.of(2026, 3, 10);
        LocalDate endDate = LocalDate.of(2026, 3, 9);

        // when · then
        assertThatThrownBy(() -> EnrollmentPeriod.of(startDate, endDate)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("[TC-1-14] 수강 기간 VO는 시작일과 종료일이 같은 하루짜리 기간을 허용한다 (경계)")
    void createSameDayPeriod() {
        // given
        LocalDate date = LocalDate.of(2026, 3, 10);

        // when
        EnrollmentPeriod period = EnrollmentPeriod.of(date, date);

        // then
        assertThat(period.getStartDate()).isEqualTo(date);
        assertThat(period.getEndDate()).isEqualTo(date);
        assertThat(period.isIndefinite()).isFalse();
    }

    @Test
    @DisplayName("[TC-1-14] 수강 기간 VO의 시작일이 null이면 IllegalArgumentException이 발생한다")
    void createWithoutStartDate() {
        // given
        LocalDate endDate = LocalDate.of(2026, 3, 31);

        // when · then
        assertThatThrownBy(() -> EnrollmentPeriod.of(null, endDate)).isInstanceOf(IllegalArgumentException.class);
    }
}
