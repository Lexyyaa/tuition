package com.academy.tuition.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.academy.tuition.support.IntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class SeedDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("[TC-1-09] data.sql 로딩 — 학원(플랜·할인 설정 포함)·학부모·수강생·강좌 seed가 02 §9 기준과 일치한다")
    void seedMatchesDesign() {
        // given: data.sql은 컨텍스트 기동 시 로딩된다 (sql.init.mode: always)

        // when
        List<Map<String, Object>> academies = jdbcTemplate.queryForList(
                "select id, name, plan, sibling_discount_rate, sibling_discount_target from academy where id in (1, 2) order by id");
        List<Map<String, Object>> parents = jdbcTemplate.queryForList(
                "select id, academy_id, name, phone, notification_refused from parent where id in (1, 2, 3) order by id");
        List<Map<String, Object>> students = jdbcTemplate.queryForList(
                "select id, parent_id, name from student where id in (1, 2, 3, 4) order by id");
        List<Map<String, Object>> courses = jdbcTemplate.queryForList(
                "select id, academy_id, name, monthly_fee, class_days, capacity from course where id in (1, 2, 3) order by id");

        // then: academy 2건 — 플랜·할인 설정 포함 대조
        assertThat(academies).hasSize(2);
        assertThat(academies.get(0)).containsEntry("name", "강남수학학원").containsEntry("plan", "PAID");
        assertThat(((Number) academies.get(0).get("sibling_discount_rate")).intValue())
                .isEqualTo(10);
        assertThat(academies.get(0)).containsEntry("sibling_discount_target", "FROM_SECOND");
        assertThat(academies.get(1)).containsEntry("name", "서초영어학원").containsEntry("plan", "FREE");
        assertThat(((Number) academies.get(1).get("sibling_discount_rate")).intValue())
                .isEqualTo(5);
        assertThat(academies.get(1)).containsEntry("sibling_discount_target", "ALL");

        // then: parent 3건 — 수신 거부는 전부 false
        assertThat(parents).hasSize(3);
        assertThat(parents.get(0)).containsEntry("name", "김학부모").containsEntry("phone", "010-1000-0001");
        assertThat(parents.get(1)).containsEntry("name", "이학부모").containsEntry("phone", "010-1000-0002");
        assertThat(parents.get(2)).containsEntry("name", "박학부모").containsEntry("phone", "010-2000-0001");
        assertThat(((Number) parents.get(0).get("academy_id")).longValue()).isEqualTo(1L);
        assertThat(((Number) parents.get(1).get("academy_id")).longValue()).isEqualTo(1L);
        assertThat(((Number) parents.get(2).get("academy_id")).longValue()).isEqualTo(2L);
        assertThat(parents)
                .allSatisfy(row ->
                        assertThat(((Boolean) row.get("notification_refused"))).isFalse());

        // then: student 4건 — 형제(1·2)는 같은 parent_id
        assertThat(students).hasSize(4);
        assertThat(students.get(0)).containsEntry("name", "김첫째");
        assertThat(students.get(1)).containsEntry("name", "김둘째");
        assertThat(students.get(2)).containsEntry("name", "이외동");
        assertThat(students.get(3)).containsEntry("name", "박외동");
        assertThat(((Number) students.get(0).get("parent_id")).longValue()).isEqualTo(1L);
        assertThat(((Number) students.get(1).get("parent_id")).longValue()).isEqualTo(1L);
        assertThat(((Number) students.get(2).get("parent_id")).longValue()).isEqualTo(2L);
        assertThat(((Number) students.get(3).get("parent_id")).longValue()).isEqualTo(3L);

        // then: course 3건 — 월 원비·요일·정원 대조
        assertThat(courses).hasSize(3);
        assertThat(courses.get(0)).containsEntry("name", "중등수학A").containsEntry("class_days", "MON,WED,FRI");
        assertThat(((Number) courses.get(0).get("monthly_fee")).longValue()).isEqualTo(300000L);
        assertThat(((Number) courses.get(0).get("capacity")).intValue()).isEqualTo(10);
        assertThat(courses.get(1)).containsEntry("name", "중등수학B").containsEntry("class_days", "TUE,THU");
        assertThat(((Number) courses.get(1).get("monthly_fee")).longValue()).isEqualTo(200000L);
        assertThat(((Number) courses.get(1).get("capacity")).intValue()).isEqualTo(1);
        assertThat(courses.get(2)).containsEntry("name", "중등영어A").containsEntry("class_days", "MON,WED,FRI");
        assertThat(((Number) courses.get(2).get("monthly_fee")).longValue()).isEqualTo(300000L);
        assertThat(((Number) courses.get(2).get("capacity")).intValue()).isEqualTo(5);
        assertThat(((Number) courses.get(0).get("academy_id")).longValue()).isEqualTo(1L);
        assertThat(((Number) courses.get(1).get("academy_id")).longValue()).isEqualTo(1L);
        assertThat(((Number) courses.get(2).get("academy_id")).longValue()).isEqualTo(2L);
    }
}
