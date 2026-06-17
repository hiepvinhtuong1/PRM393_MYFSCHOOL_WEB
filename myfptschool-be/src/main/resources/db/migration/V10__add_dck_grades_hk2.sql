-- V10: Thêm điểm cuối kỳ (DCK, component_id=5) cho HK II 2025-2026
-- V9 chỉ seed TX1/TX2/DGKK, thiếu DCK nên subjectAverage = null và hiển thị "Đang học"
-- Kỳ thi cuối kỳ HK II được tổ chức vào 20-25/05/2026

INSERT INTO grade_records (student_id, classroom_subject_id, component_id, score, recorded_by, recorded_at)
SELECT DISTINCT ON (gr.student_id, gr.classroom_subject_id)
    gr.student_id,
    gr.classroom_subject_id,
    5 AS component_id,
    ROUND(CAST(5.5 + ((gr.student_id * 7 + gr.classroom_subject_id * 11) % 45) / 10.0 AS NUMERIC), 1) AS score,
    gr.recorded_by,
    '2026-05-25 09:00:00' AS recorded_at
FROM grade_records gr
JOIN classroom_subjects cs ON cs.id = gr.classroom_subject_id
JOIN semesters s ON s.id = cs.semester_id
JOIN academic_years ay ON ay.id = s.academic_year_id
WHERE ay.label = '2025-2026'
  AND s.name = 'HK II'
  AND NOT EXISTS (
      SELECT 1 FROM grade_records dup
      WHERE dup.student_id = gr.student_id
        AND dup.classroom_subject_id = gr.classroom_subject_id
        AND dup.component_id = 5
  );
