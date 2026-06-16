ALTER TABLE classrooms
    ADD COLUMN IF NOT EXISTS homeroom_teacher_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'classrooms'
          AND constraint_name = 'fk_classroom_homeroom_teacher'
    ) THEN
        ALTER TABLE classrooms
            ADD CONSTRAINT fk_classroom_homeroom_teacher
                FOREIGN KEY (homeroom_teacher_id) REFERENCES teachers (id)
                ON DELETE SET NULL;
    END IF;
END $$;
