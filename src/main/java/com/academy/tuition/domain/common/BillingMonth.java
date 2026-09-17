package com.academy.tuition.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고지 월 VO. yyyy-MM 문자열로 보관한다 (02 §1, 예: 2026-03).
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BillingMonth {

    private static final Pattern FORMAT = Pattern.compile("^\\d{4}-\\d{2}$");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Column(length = 7, nullable = false)
    private String value;

    private BillingMonth(String value) {
        this.value = value;
    }

    public static BillingMonth of(String value) {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("고지 월은 yyyy-MM 형식이어야 합니다: " + value);
        }
        try {
            YearMonth.parse(value, FORMATTER);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("고지 월은 yyyy-MM 형식이어야 합니다: " + value, e);
        }
        return new BillingMonth(value);
    }

    public static BillingMonth of(YearMonth yearMonth) {
        if (yearMonth == null) {
            throw new IllegalArgumentException("고지 월은 필수입니다");
        }
        return new BillingMonth(yearMonth.format(FORMATTER));
    }

    public YearMonth toYearMonth() {
        return YearMonth.parse(value, FORMATTER);
    }
}
