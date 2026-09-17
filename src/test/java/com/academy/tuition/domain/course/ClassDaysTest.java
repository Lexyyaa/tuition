package com.academy.tuition.domain.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClassDaysTest {

    @Test
    @DisplayName("[TC-1-05] 수업 요일 VO를 [월, 수, 금]으로 생성하면 요일 집합을 보관한다")
    void createClassDays() {
        // given
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

        // when
        ClassDays classDays = ClassDays.of(days);

        // then
        assertThat(classDays.days()).containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        assertThat(classDays.size()).isEqualTo(3);
        assertThat(classDays.contains(DayOfWeek.MONDAY)).isTrue();
        assertThat(classDays.contains(DayOfWeek.TUESDAY)).isFalse();
    }

    @Test
    @DisplayName("[TC-1-06] 수업 요일 VO를 빈 목록으로 생성하면 IllegalArgumentException이 발생한다")
    void createEmptyClassDays() {
        // given
        List<DayOfWeek> empty = List.of();

        // when · then
        assertThatThrownBy(() -> ClassDays.of(empty)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("[TC-1-13] 수업 요일 목록에 중복이 있으면 IllegalArgumentException이 발생한다")
    void createDuplicatedClassDays() {
        // given
        List<DayOfWeek> duplicated = List.of(DayOfWeek.MONDAY, DayOfWeek.MONDAY);

        // when · then
        assertThatThrownBy(() -> ClassDays.of(duplicated)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("[TC-1-13] 저장 문자열은 MON,WED,FRI 형식으로 자연 순서를 유지하고 왕복 변환된다")
    void storageStringRoundTrip() {
        // given — 입력 순서가 뒤섞여도 저장은 자연 순서다
        ClassDays classDays = ClassDays.of(List.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        // when
        String stored = classDays.toStorageString();
        ClassDays restored = ClassDays.fromStorageString(stored);

        // then
        assertThat(stored).isEqualTo("MON,WED,FRI");
        assertThat(restored).isEqualTo(classDays);
    }

    @Test
    @DisplayName("[TC-1-13] 알 수 없는 요일 토큰의 저장 문자열은 IllegalArgumentException이 발생한다")
    void fromInvalidStorageString() {
        // given
        String invalid = "MON,XXX";

        // when · then
        assertThatThrownBy(() -> ClassDays.fromStorageString(invalid)).isInstanceOf(IllegalArgumentException.class);
    }
}
