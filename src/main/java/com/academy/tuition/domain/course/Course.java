package com.academy.tuition.domain.course;

import com.academy.tuition.domain.common.BaseTimeEntity;
import com.academy.tuition.domain.common.Money;
import com.academy.tuition.domain.course.exception.CourseException;
import com.academy.tuition.domain.exception.ErrorCode;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.util.Collection;
import java.util.HashSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강좌 애그리거트 (02 §3.3). 강좌명 · 월 원비 · 수업 요일 · 정원.
 */
@Getter
@Entity
@Table(name = "course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {

    private static final int MIN_CAPACITY = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long academyId;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "monthly_fee", nullable = false))
    private Money monthlyFee;

    @Convert(converter = ClassDaysConverter.class)
    @Column(name = "class_days", nullable = false)
    private ClassDays classDays;

    @Column(nullable = false)
    private int capacity;

    private Course(Long academyId, String name, Money monthlyFee, ClassDays classDays, int capacity) {
        this.academyId = academyId;
        this.name = name;
        this.monthlyFee = monthlyFee;
        this.classDays = classDays;
        this.capacity = capacity;
    }

    public static Course create(
            Long academyId, String name, long monthlyFee, Collection<DayOfWeek> classDays, int capacity) {
        if (academyId == null) {
            throw new CourseException(ErrorCode.INVALID_INPUT, "academyId: 학원 식별자는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new CourseException(ErrorCode.INVALID_INPUT, "name: 강좌명은 필수입니다");
        }
        if (monthlyFee < 0) {
            throw new CourseException(ErrorCode.INVALID_INPUT, "monthlyFee: 월 원비는 0 이상이어야 합니다");
        }
        if (classDays == null || classDays.isEmpty()) {
            throw new CourseException(ErrorCode.INVALID_INPUT, "classDays: 수업 요일은 1개 이상이어야 합니다");
        }
        if (new HashSet<>(classDays).size() != classDays.size()) {
            throw new CourseException(ErrorCode.INVALID_INPUT, "classDays: 수업 요일은 중복될 수 없습니다");
        }
        if (capacity < MIN_CAPACITY) {
            throw new CourseException(ErrorCode.INVALID_INPUT, "capacity: 정원은 1 이상이어야 합니다");
        }
        return new Course(academyId, name, Money.of(monthlyFee), ClassDays.of(classDays), capacity);
    }
}
