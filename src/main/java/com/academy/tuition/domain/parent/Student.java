package com.academy.tuition.domain.parent;

import com.academy.tuition.domain.common.BaseTimeEntity;
import com.academy.tuition.domain.exception.ErrorCode;
import com.academy.tuition.domain.parent.exception.ParentException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수강생 — Parent 애그리거트의 하위 엔티티 (02 §3.2).
 * 이름은 02 §1 용어 표에서 Student로 고정됐다.
 */
@Getter
@Entity
@Table(name = "student")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Student(String name) {
        this.name = name;
    }

    public static Student of(String name) {
        if (name == null || name.isBlank()) {
            throw new ParentException(ErrorCode.INVALID_INPUT, "students.name: 수강생 이름은 필수입니다");
        }
        return new Student(name);
    }
}
