package com.academy.tuition.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PaymentDayTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 31})
    @DisplayName("[TC-1-03] 납부일 VO를 1·31로 생성하면 정상 생성된다 (경계)")
    void createBoundaryPaymentDay(int day) {
        // given · when
        PaymentDay paymentDay = PaymentDay.of(day);

        // then
        assertThat(paymentDay.getValue()).isEqualTo(day);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 32})
    @DisplayName("[TC-1-04] 납부일 VO를 0·32로 생성하면 IllegalArgumentException이 발생한다")
    void createOutOfRangePaymentDay(int day) {
        // given · when · then
        assertThatThrownBy(() -> PaymentDay.of(day)).isInstanceOf(IllegalArgumentException.class);
    }
}
