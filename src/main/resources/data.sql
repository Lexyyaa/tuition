-- 기초 데이터 (docs/design/02-domain-model.md §9와 행 단위 대조 — TC-1-09)
-- BaseTimeEntity 컬럼(created_at·updated_at)은 NOT NULL이라 NOW(6)를 넣는다
-- 기동마다 실행되므로 INSERT IGNORE로 쓴다

-- academy (2건)
INSERT IGNORE INTO academy (id, name, plan, sibling_discount_rate, sibling_discount_target, created_at, updated_at) VALUES
    (1, '강남수학학원', 'PAID', 10, 'FROM_SECOND', NOW(6), NOW(6)),
    (2, '서초영어학원', 'FREE', 5, 'ALL', NOW(6), NOW(6));

-- parent (3건) — 수신 거부는 전부 false, 변경은 API(FR-2.11)로 검증
INSERT IGNORE INTO parent (id, academy_id, name, phone, notification_refused, created_at, updated_at) VALUES
    (1, 1, '김학부모', '010-1000-0001', false, NOW(6), NOW(6)),
    (2, 1, '이학부모', '010-1000-0002', false, NOW(6), NOW(6)),
    (3, 2, '박학부모', '010-2000-0001', false, NOW(6), NOW(6));

-- student (4건)
INSERT IGNORE INTO student (id, parent_id, name, created_at, updated_at) VALUES
    (1, 1, '김첫째', NOW(6), NOW(6)),
    (2, 1, '김둘째', NOW(6), NOW(6)),
    (3, 2, '이외동', NOW(6), NOW(6)),
    (4, 3, '박외동', NOW(6), NOW(6));

-- course (3건)
INSERT IGNORE INTO course (id, academy_id, name, monthly_fee, class_days, capacity, created_at, updated_at) VALUES
    (1, 1, '중등수학A', 300000, 'MON,WED,FRI', 10, NOW(6), NOW(6)),
    (2, 1, '중등수학B', 200000, 'TUE,THU', 1, NOW(6), NOW(6)),
    (3, 2, '중등영어A', 300000, 'MON,WED,FRI', 5, NOW(6), NOW(6));
