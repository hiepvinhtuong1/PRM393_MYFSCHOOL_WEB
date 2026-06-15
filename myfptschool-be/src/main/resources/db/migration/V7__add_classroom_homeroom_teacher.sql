ALTER TABLE classrooms
    ADD COLUMN homeroom_teacher_id BIGINT,
    ADD CONSTRAINT fk_classroom_homeroom_teacher
        FOREIGN KEY (homeroom_teacher_id) REFERENCES teachers (id)
        ON DELETE SET NULL;
