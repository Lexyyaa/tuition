package com.academy.tuition.domain.course;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;

/**
 * 수업 요일 VO. 요일 집합, 빈 집합 불가 (02 §1).
 * 저장 형식은 자연 순서(MON..SUN)의 3글자 토큰 "MON,WED,FRI" (02 §9 seed와 동일).
 */
@EqualsAndHashCode
public class ClassDays {

    private static final String DELIMITER = ",";

    private final Set<DayOfWeek> days;

    private ClassDays(Set<DayOfWeek> days) {
        this.days = Collections.unmodifiableSet(days);
    }

    public static ClassDays of(Collection<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("수업 요일은 1개 이상이어야 합니다");
        }
        if (days.stream().anyMatch(day -> day == null)) {
            throw new IllegalArgumentException("수업 요일에 null은 올 수 없습니다");
        }
        Set<DayOfWeek> distinct = EnumSet.copyOf(days);
        if (distinct.size() != days.size()) {
            throw new IllegalArgumentException("수업 요일은 중복될 수 없습니다: " + days);
        }
        return new ClassDays(distinct);
    }

    public static ClassDays fromStorageString(String stored) {
        if (stored == null || stored.isBlank()) {
            throw new IllegalArgumentException("수업 요일 저장 값이 비어 있습니다");
        }
        Set<DayOfWeek> parsed = EnumSet.noneOf(DayOfWeek.class);
        for (String token : stored.split(DELIMITER)) {
            parsed.add(parseToken(token.trim()));
        }
        return of(parsed);
    }

    public String toStorageString() {
        return days.stream().map(ClassDays::toToken).collect(Collectors.joining(DELIMITER));
    }

    public Set<DayOfWeek> days() {
        return days;
    }

    public boolean contains(DayOfWeek day) {
        return days.contains(day);
    }

    public int size() {
        return days.size();
    }

    private static String toToken(DayOfWeek day) {
        return day.name().substring(0, 3);
    }

    private static DayOfWeek parseToken(String token) {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (toToken(day).equals(token)) {
                return day;
            }
        }
        throw new IllegalArgumentException("알 수 없는 요일 토큰입니다: " + token);
    }
}
