-- =============================================================================
-- V1: Khởi tạo toàn bộ schema
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- NHÓM 1: IDENTITY & AUTH

CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL
                      CHECK (role IN ('STUDENT', 'PARENT', 'TEACHER', 'HOMEROOM_TEACHER', 'ADMIN')),
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE user_sessions (
    id         BIGSERIAL   PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      TEXT        NOT NULL UNIQUE,
    platform   VARCHAR(10) NOT NULL CHECK (platform IN ('mobile', 'web')),
    expires_at TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE campuses (
    id      BIGSERIAL    PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    address TEXT,
    phone   VARCHAR(20),
    email   VARCHAR(100),
    website VARCHAR(100)
);

CREATE TABLE teachers (
    id        BIGSERIAL    PRIMARY KEY,
    user_id   BIGINT       UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    full_name VARCHAR(100) NOT NULL,
    phone     VARCHAR(20),
    email     VARCHAR(100),
    campus_id BIGINT       REFERENCES campuses(id)
);

CREATE TABLE academic_years (
    id         BIGSERIAL   PRIMARY KEY,
    label      VARCHAR(20) NOT NULL UNIQUE,
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL
);

CREATE TABLE semesters (
    id               BIGSERIAL   PRIMARY KEY,
    academic_year_id BIGINT      NOT NULL REFERENCES academic_years(id),
    name             VARCHAR(10) NOT NULL CHECK (name IN ('HK I', 'HK II')),
    start_date       DATE        NOT NULL,
    end_date         DATE        NOT NULL,
    UNIQUE (academic_year_id, name)
);

CREATE TABLE classrooms (
    id                  BIGSERIAL   PRIMARY KEY,
    name                VARCHAR(20) NOT NULL,
    grade_level         SMALLINT    NOT NULL CHECK (grade_level IN (10, 11, 12)),
    campus_id           BIGINT      NOT NULL REFERENCES campuses(id),
    homeroom_teacher_id BIGINT      REFERENCES teachers(id),
    academic_year_id    BIGINT      NOT NULL REFERENCES academic_years(id),
    UNIQUE (name, academic_year_id, campus_id)
);

CREATE TABLE students (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    student_code  VARCHAR(20)  NOT NULL UNIQUE,
    full_name     VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender        VARCHAR(10)  CHECK (gender IN ('Nam', 'Nữ', 'Khác')),
    phone         VARCHAR(20),
    email         VARCHAR(100),
    classroom_id  BIGINT       REFERENCES classrooms(id),
    campus_id     BIGINT       REFERENCES campuses(id)
);

CREATE TABLE parents (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    parent_code   VARCHAR(20)  NOT NULL UNIQUE,
    full_name     VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender        VARCHAR(10)  CHECK (gender IN ('Nam', 'Nữ', 'Khác')),
    phone         VARCHAR(20),
    email         VARCHAR(100)
);

CREATE TABLE parent_students (
    parent_id  BIGINT NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    PRIMARY KEY (parent_id, student_id)
);

-- NHÓM 2: CẤU TRÚC HỌC THUẬT

CREATE TABLE subjects (
    id                    BIGSERIAL   PRIMARY KEY,
    name                  VARCHAR(50) NOT NULL,
    color_hex             VARCHAR(7),
    sessions_per_semester SMALLINT
);

CREATE TABLE classroom_subjects (
    id           BIGSERIAL PRIMARY KEY,
    classroom_id BIGINT    NOT NULL REFERENCES classrooms(id),
    subject_id   BIGINT    NOT NULL REFERENCES subjects(id),
    teacher_id   BIGINT    NOT NULL REFERENCES teachers(id),
    semester_id  BIGINT    NOT NULL REFERENCES semesters(id),
    UNIQUE (classroom_id, subject_id, semester_id)
);

-- NHÓM 3: THỜI KHÓA BIỂU

CREATE TABLE time_slots (
    id          SMALLSERIAL PRIMARY KEY,
    slot_number SMALLINT    NOT NULL UNIQUE,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL
);

CREATE TABLE rooms (
    id        BIGSERIAL   PRIMARY KEY,
    code      VARCHAR(30) NOT NULL,
    campus_id BIGINT      NOT NULL REFERENCES campuses(id),
    UNIQUE (code, campus_id)
);

CREATE TABLE lessons (
    id                   BIGSERIAL   PRIMARY KEY,
    classroom_subject_id BIGINT      NOT NULL REFERENCES classroom_subjects(id),
    lesson_date          DATE        NOT NULL,
    start_slot_id        SMALLINT    NOT NULL REFERENCES time_slots(id),
    end_slot_id          SMALLINT    NOT NULL REFERENCES time_slots(id),
    room_id              BIGINT      REFERENCES rooms(id),
    status               VARCHAR(20) NOT NULL DEFAULT 'scheduled'
                             CHECK (status IN ('scheduled', 'completed', 'cancelled', 'makeup')),
    has_materials        BOOLEAN     NOT NULL DEFAULT false,
    note                 TEXT,
    UNIQUE (classroom_subject_id, lesson_date, start_slot_id)
);

-- NHÓM 4: ĐIỂM DANH

CREATE TABLE attendance_records (
    id          BIGSERIAL   PRIMARY KEY,
    student_id  BIGINT      NOT NULL REFERENCES students(id),
    lesson_id   BIGINT      NOT NULL REFERENCES lessons(id),
    status      VARCHAR(20) NOT NULL
                    CHECK (status IN ('present', 'late', 'excused_absent', 'unexcused_absent')),
    note        TEXT,
    recorded_by BIGINT      REFERENCES users(id),
    recorded_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, lesson_id)
);

-- NHÓM 5: ĐIỂM SỐ

CREATE TABLE score_components (
    id            SMALLSERIAL PRIMARY KEY,
    code          VARCHAR(10) NOT NULL UNIQUE,
    name          VARCHAR(50) NOT NULL,
    weight        SMALLINT    NOT NULL,
    display_order SMALLINT    NOT NULL
);

CREATE TABLE grade_records (
    id                   BIGSERIAL     PRIMARY KEY,
    student_id           BIGINT        NOT NULL REFERENCES students(id),
    classroom_subject_id BIGINT        NOT NULL REFERENCES classroom_subjects(id),
    component_id         SMALLINT      NOT NULL REFERENCES score_components(id),
    score                NUMERIC(4, 2) CHECK (score >= 0 AND score <= 10),
    recorded_by          BIGINT        REFERENCES users(id),
    recorded_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, classroom_subject_id, component_id)
);

-- NHÓM 6: THÔNG BÁO

CREATE TABLE notifications (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    body        TEXT         NOT NULL,
    category    VARCHAR(20)  NOT NULL
                    CHECK (category IN ('attendance', 'grade', 'homeroom', 'study', 'event')),
    target_type VARCHAR(20)  NOT NULL
                    CHECK (target_type IN ('individual', 'classroom', 'all')),
    target_id   BIGINT,
    created_by  BIGINT       REFERENCES users(id),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE notification_recipients (
    id              BIGSERIAL PRIMARY KEY,
    notification_id BIGINT    NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    user_id         BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_read         BOOLEAN   NOT NULL DEFAULT false,
    read_at         TIMESTAMP,
    UNIQUE (notification_id, user_id)
);

-- INDEXES

CREATE INDEX idx_user_sessions_token    ON user_sessions(token);
CREATE INDEX idx_user_sessions_user     ON user_sessions(user_id);
CREATE INDEX idx_lessons_date           ON lessons(lesson_date);
CREATE INDEX idx_lessons_cs_date        ON lessons(classroom_subject_id, lesson_date);
CREATE INDEX idx_attendance_student     ON attendance_records(student_id);
CREATE INDEX idx_attendance_lesson      ON attendance_records(lesson_id);
CREATE INDEX idx_grade_student          ON grade_records(student_id);
CREATE INDEX idx_grade_cs               ON grade_records(classroom_subject_id);
CREATE INDEX idx_notif_recipients_user  ON notification_recipients(user_id, is_read);

-- VIEWS

CREATE VIEW attendance_summary AS
SELECT
    ar.student_id,
    cs.subject_id,
    cs.semester_id,
    cs.classroom_id,
    COUNT(*)                                                                  AS total_sessions,
    COUNT(*) FILTER (WHERE ar.status = 'present')                            AS present_sessions,
    COUNT(*) FILTER (WHERE ar.status = 'late')                               AS late_sessions,
    COUNT(*) FILTER (WHERE ar.status = 'excused_absent')                     AS excused_absent,
    COUNT(*) FILTER (WHERE ar.status = 'unexcused_absent')                   AS unexcused_absent,
    COUNT(*) FILTER (WHERE ar.status IN ('excused_absent','unexcused_absent')) AS total_absent
FROM attendance_records ar
JOIN lessons            l  ON ar.lesson_id            = l.id
JOIN classroom_subjects cs ON l.classroom_subject_id  = cs.id
GROUP BY ar.student_id, cs.subject_id, cs.semester_id, cs.classroom_id;

CREATE VIEW grade_summary AS
SELECT
    gr.student_id,
    gr.classroom_subject_id,
    cs.subject_id,
    cs.semester_id,
    cs.classroom_id,
    ROUND(SUM(gr.score * sc.weight) / NULLIF(SUM(sc.weight), 0), 1) AS final_grade,
    COUNT(gr.id)                                                      AS components_filled,
    (SELECT COUNT(*) FROM score_components)                           AS components_total
FROM grade_records      gr
JOIN score_components   sc ON gr.component_id         = sc.id
JOIN classroom_subjects cs ON gr.classroom_subject_id = cs.id
GROUP BY gr.student_id, gr.classroom_subject_id, cs.subject_id, cs.semester_id, cs.classroom_id;
