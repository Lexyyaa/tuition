package com.academy.tuition.domain.parent;

import com.academy.tuition.domain.common.BaseTimeEntity;
import com.academy.tuition.domain.exception.ErrorCode;
import com.academy.tuition.domain.parent.exception.ParentException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학부모 애그리거트 (02 §3.2). 전화번호 · 수신 거부는 학부모에만 있다 (D-6).
 * 형제 판별은 같은 parentId 기준 (D-4).
 */
@Getter
@Entity
@Table(name = "parent")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long academyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private boolean notificationRefused;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id", nullable = false)
    @OrderBy("id ASC")
    private List<Student> students = new ArrayList<>();

    private Parent(Long academyId, String name, String phone, List<Student> students) {
        this.academyId = academyId;
        this.name = name;
        this.phone = phone;
        this.notificationRefused = false;
        this.students = new ArrayList<>(students);
    }

    public static Parent create(Long academyId, String name, String phone, List<Student> students) {
        if (academyId == null) {
            throw new ParentException(ErrorCode.INVALID_INPUT, "academyId: 학원 식별자는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new ParentException(ErrorCode.INVALID_INPUT, "name: 학부모 이름은 필수입니다");
        }
        if (phone == null || phone.isBlank()) {
            throw new ParentException(ErrorCode.INVALID_INPUT, "phone: 전화번호는 필수입니다");
        }
        if (students == null) {
            throw new ParentException(ErrorCode.INVALID_INPUT, "students: 수강생 목록은 필수입니다");
        }
        return new Parent(academyId, name, phone, students);
    }

    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }
}
