package com.academy.tuition.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("[TC-1-01] 금액 VO를 300,000원으로 생성하면 원 단위 정수로 보관한다")
    void createMoney() {
        // given
        long amount = 300_000L;

        // when
        Money money = Money.of(amount);

        // then
        assertThat(money.getAmount()).isEqualTo(300_000L);
    }

    @Test
    @DisplayName("[TC-1-02] 금액 VO를 음수로 생성하면 IllegalArgumentException이 발생한다")
    void createNegativeMoney() {
        // given
        long negative = -1L;

        // when · then
        assertThatThrownBy(() -> Money.of(negative)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("[TC-1-12] 금액 VO를 0원으로 생성할 수 있다 (경계)")
    void createZeroMoney() {
        // given
        long zero = 0L;

        // when
        Money money = Money.of(zero);

        // then
        assertThat(money.getAmount()).isZero();
    }

    @Test
    @DisplayName("[TC-1-12] 금액이 같으면 같은 값으로 판정한다")
    void equality() {
        // given
        Money a = Money.of(1_000L);
        Money b = Money.of(1_000L);

        // when · then
        assertThat(a).isEqualTo(b);
    }
}
