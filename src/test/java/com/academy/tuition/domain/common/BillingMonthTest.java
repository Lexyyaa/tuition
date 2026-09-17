package com.academy.tuition.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BillingMonthTest {

    @Test
    @DisplayName("고지 월 VO를 yyyy-MM 문자열로 생성하면 값을 그대로 보관한다")
    void createBillingMonth() {
        // given
        String value = "2026-03";

        // when
        BillingMonth billingMonth = BillingMonth.of(value);

        // then
        assertThat(billingMonth.getValue()).isEqualTo("2026-03");
        assertThat(billingMonth.toYearMonth()).isEqualTo(YearMonth.of(2026, 3));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-3", "202603", "2026-13", "2026-00", "2026/03"})
    @DisplayName("고지 월 VO를 yyyy-MM 형식이 아닌 값으로 생성하면 IllegalArgumentException이 발생한다")
    void createInvalidBillingMonth(String invalid) {
        // given · when · then
        assertThatThrownBy(() -> BillingMonth.of(invalid)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("고지 월 VO를 YearMonth로 생성하면 yyyy-MM 문자열로 변환해 보관한다")
    void createFromYearMonth() {
        // given
        YearMonth yearMonth = YearMonth.of(2026, 3);

        // when
        BillingMonth billingMonth = BillingMonth.of(yearMonth);

        // then
        assertThat(billingMonth.getValue()).isEqualTo("2026-03");
    }
}
