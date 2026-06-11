-- =============================================================================
-- V3: Thêm created_at và updated_at vào tất cả các bảng entity
-- =============================================================================

-- Bảng chưa có created_at và updated_at
ALTER TABLE campuses
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE teachers
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE academic_years
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE semesters
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE classrooms
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE students
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE parents
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE subjects
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE classroom_subjects
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE time_slots
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE rooms
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE lessons
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE attendance_records
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE score_components
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE grade_records
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE notification_recipients
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- Bảng đã có created_at, chỉ cần thêm updated_at
ALTER TABLE user_sessions
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE notifications
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- Triggers cho updated_at (dùng lại function set_updated_at() đã tạo ở V1)
CREATE TRIGGER trg_campuses_updated_at
    BEFORE UPDATE ON campuses FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_teachers_updated_at
    BEFORE UPDATE ON teachers FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_academic_years_updated_at
    BEFORE UPDATE ON academic_years FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_semesters_updated_at
    BEFORE UPDATE ON semesters FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_classrooms_updated_at
    BEFORE UPDATE ON classrooms FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_students_updated_at
    BEFORE UPDATE ON students FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_parents_updated_at
    BEFORE UPDATE ON parents FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_subjects_updated_at
    BEFORE UPDATE ON subjects FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_classroom_subjects_updated_at
    BEFORE UPDATE ON classroom_subjects FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_time_slots_updated_at
    BEFORE UPDATE ON time_slots FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_rooms_updated_at
    BEFORE UPDATE ON rooms FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_lessons_updated_at
    BEFORE UPDATE ON lessons FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_attendance_records_updated_at
    BEFORE UPDATE ON attendance_records FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_score_components_updated_at
    BEFORE UPDATE ON score_components FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_grade_records_updated_at
    BEFORE UPDATE ON grade_records FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_notification_recipients_updated_at
    BEFORE UPDATE ON notification_recipients FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_user_sessions_updated_at
    BEFORE UPDATE ON user_sessions FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_notifications_updated_at
    BEFORE UPDATE ON notifications FOR EACH ROW EXECUTE FUNCTION set_updated_at();
