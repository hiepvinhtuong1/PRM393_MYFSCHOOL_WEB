-- =============================================================================
-- V8: Dữ liệu mẫu demo
--   - 2 campus, 2 năm học, 4 học kỳ
--   - 10 môn học, 10 phòng học
--   - 15 giáo viên (6 GVCN + 9 GV bộ môn)
--   - 6 lớp học (năm học 2025-2026)
--   - 60 học sinh, 30 phụ huynh
--   - 120 classroom_subjects (6 lớp × 10 môn × 2 kỳ)
--   - ~1 500 tiết học, ~15 000 điểm danh, ~4 800 điểm số
--   - Thông báo + người nhận
-- =============================================================================

DO $seed$
DECLARE
  c_hn          BIGINT;
  c_hcm         BIGINT;
  ay1           BIGINT;
  ay2           BIGINT;
  sem1          BIGINT;   -- 2025-2026 HK I
  sem2          BIGINT;   -- 2025-2026 HK II
  sem3          BIGINT;   -- 2026-2027 HK I
  sem4          BIGINT;   -- 2026-2027 HK II

  sub_ids       BIGINT[];
  room_ids      BIGINT[];
  t_ids         BIGINT[];
  t_user_ids    BIGINT[];
  cl_ids        BIGINT[];
  stu_ids       BIGINT[];
  cs_hk1        BIGINT[];
  cs_hk2        BIGINT[];

  uid           BIGINT;
  rid           BIGINT;
  cid           BIGINT;
  csid          BIGINT;
  lid           BIGINT;
  par_uid       BIGINT;
  par_id        BIGINT;
  n_id          BIGINT;
  admin_uid     BIGINT;

  i             INT;
  j             INT;
  k             INT;
  s             INT;

  lesson_date   DATE;
  dow           INT;
  slot_s        SMALLINT;
  slot_e        SMALLINT;
  sub_wc        INT;
  cl_offset     INT;

  stu_full_name TEXT;
  par_full_name TEXT;
  att_status    TEXT;

  teacher_names TEXT[];
  teacher_roles TEXT[];
  first_names   TEXT[];
  last_names    TEXT[];
  cl_names      TEXT[];
  cl_grades     SMALLINT[];
  sub_names     TEXT[];
  sub_colors    TEXT[];
  sub_sessions  SMALLINT[];
  sub_coefs     SMALLINT[];
BEGIN

-- ================================================================
-- Bỏ qua nếu đã seed
-- ================================================================
IF (SELECT COUNT(*) FROM campuses) > 0 THEN
  RAISE NOTICE 'V8: campuses đã tồn tại, bỏ qua seed.';
  RETURN;
END IF;

-- ================================================================
-- Khởi tạo mảng hằng số
-- ================================================================
teacher_names := ARRAY[
  'Nguyễn Văn An',   'Trần Thị Bích',  'Lê Văn Cường',
  'Phạm Thị Dung',   'Hoàng Văn Em',   'Đỗ Thị Phương',
  'Vũ Văn Giang',    'Nguyễn Thị Hoa', 'Bùi Văn Hùng',
  'Trương Thị Lan',  'Võ Văn Minh',    'Ngô Thị Nga',
  'Dương Văn Phúc',  'Lý Thị Quyên',   'Đinh Văn Sơn'
];
-- Giáo viên 1-6: GVCN; 7-15: GV bộ môn
teacher_roles := ARRAY[
  'HOMEROOM_TEACHER','HOMEROOM_TEACHER','HOMEROOM_TEACHER',
  'HOMEROOM_TEACHER','HOMEROOM_TEACHER','HOMEROOM_TEACHER',
  'TEACHER','TEACHER','TEACHER','TEACHER','TEACHER',
  'TEACHER','TEACHER','TEACHER','TEACHER'
];

first_names := ARRAY[
  'Anh','Bình','Chi','Dũng','Hà',
  'Hùng','Lan','Long','Mai','Minh',
  'Nam','Ngọc','Nhung','Phúc','Quân',
  'Sơn','Thảo','Trang','Tuấn','Vy'
];
last_names := ARRAY[
  'Nguyễn','Trần','Lê','Phạm','Hoàng',
  'Đỗ','Bùi','Vũ','Đặng','Võ'
];

cl_names  := ARRAY['10A1','10A2','11B1','11B2','12C1','12C2'];
cl_grades := ARRAY[10, 10, 11, 11, 12, 12]::SMALLINT[];

sub_names := ARRAY[
  'Toán','Ngữ Văn','Tiếng Anh','Vật Lý','Hóa Học',
  'Sinh Học','Lịch Sử','Địa Lý','Giáo dục Công dân','Tin Học'
];
sub_colors := ARRAY[
  '#E74C3C','#8E44AD','#2980B9','#27AE60','#F39C12',
  '#16A085','#D35400','#2C3E50','#7F8C8D','#1ABC9C'
];
sub_sessions := ARRAY[45,45,45,36,36,36,27,27,27,27]::SMALLINT[];
sub_coefs    := ARRAY[2, 2, 1, 1, 1, 1, 1, 1, 1, 1]::SMALLINT[];

sub_ids    := ARRAY[]::BIGINT[];
room_ids   := ARRAY[]::BIGINT[];
t_ids      := ARRAY[]::BIGINT[];
t_user_ids := ARRAY[]::BIGINT[];
cl_ids     := ARRAY[]::BIGINT[];
stu_ids    := ARRAY[]::BIGINT[];
cs_hk1     := ARRAY[]::BIGINT[];
cs_hk2     := ARRAY[]::BIGINT[];

-- ================================================================
-- 1. CAMPUS
-- ================================================================
INSERT INTO campuses (name, address, phone, email, website) VALUES
  ('FPT Hà Nội',
   'Khu Giáo dục FPT, Phường Trịnh Văn Bô, Quận Nam Từ Liêm, Hà Nội',
   '024.7300.1866', 'fpt.hn@fschool.edu.vn', 'https://fschool.fpt.edu.vn')
RETURNING id INTO c_hn;

INSERT INTO campuses (name, address, phone, email, website) VALUES
  ('FPT TP. Hồ Chí Minh',
   'Lô E2a-7, Đường D1, Khu Công nghệ Cao, P. Long Thạnh Mỹ, TP. Thủ Đức, TP.HCM',
   '028.7300.1866', 'fpt.hcm@fschool.edu.vn', 'https://fschool.fpt.edu.vn')
RETURNING id INTO c_hcm;

-- ================================================================
-- 2. NĂM HỌC
-- ================================================================
INSERT INTO academic_years (label, start_date, end_date)
VALUES ('2025-2026', '2025-09-01', '2026-05-30') RETURNING id INTO ay1;

INSERT INTO academic_years (label, start_date, end_date)
VALUES ('2026-2027', '2026-09-01', '2027-05-29') RETURNING id INTO ay2;

-- ================================================================
-- 3. HỌC KỲ
-- ================================================================
INSERT INTO semesters (academic_year_id, name, start_date, end_date)
VALUES (ay1, 'HK I', '2025-09-01', '2025-12-20') RETURNING id INTO sem1;

INSERT INTO semesters (academic_year_id, name, start_date, end_date)
VALUES (ay1, 'HK II', '2026-01-05', '2026-05-30') RETURNING id INTO sem2;

INSERT INTO semesters (academic_year_id, name, start_date, end_date)
VALUES (ay2, 'HK I', '2026-09-01', '2026-12-19') RETURNING id INTO sem3;

INSERT INTO semesters (academic_year_id, name, start_date, end_date)
VALUES (ay2, 'HK II', '2027-01-04', '2027-05-29') RETURNING id INTO sem4;

-- ================================================================
-- 4. MÔN HỌC
-- ================================================================
FOR i IN 1..10 LOOP
  INSERT INTO subjects (name, color_hex, sessions_per_semester, coefficient)
  VALUES (sub_names[i], sub_colors[i], sub_sessions[i], sub_coefs[i])
  RETURNING id INTO csid;
  sub_ids := array_append(sub_ids, csid);
END LOOP;

-- ================================================================
-- 5. PHÒNG HỌC (10 phòng tại HN)
-- ================================================================
FOR i IN 1..5 LOOP
  INSERT INTO rooms (code, campus_id)
  VALUES ('A1.' || LPAD(i::TEXT, 2, '0'), c_hn) RETURNING id INTO rid;
  room_ids := array_append(room_ids, rid);
END LOOP;
FOR i IN 1..5 LOOP
  INSERT INTO rooms (code, campus_id)
  VALUES ('B2.' || LPAD(i::TEXT, 2, '0'), c_hn) RETURNING id INTO rid;
  room_ids := array_append(room_ids, rid);
END LOOP;

-- ================================================================
-- 6. ADMIN
-- ================================================================
INSERT INTO users (username, password_hash, role)
VALUES ('admin', crypt('Password@1', gen_salt('bf', 10)), 'ADMIN')
RETURNING id INTO admin_uid;

-- ================================================================
-- 7. GIÁO VIÊN (15 người)
-- Phân công môn: t_ids[j] dạy sub_ids[j] (j=1..10)
-- t_ids[1..6]: GVCN cho cl_ids[1..6]
-- ================================================================
FOR i IN 1..15 LOOP
  INSERT INTO users (username, password_hash, role)
  VALUES (
    'teacher' || LPAD(i::TEXT, 2, '0'),
    crypt('Password@1', gen_salt('bf', 10)),
    teacher_roles[i]
  ) RETURNING id INTO uid;
  t_user_ids := array_append(t_user_ids, uid);

  INSERT INTO teachers (user_id, full_name, phone, email, campus_id)
  VALUES (
    uid,
    teacher_names[i],
    '0912' || LPAD((100000 + i)::TEXT, 6, '0'),
    'teacher' || LPAD(i::TEXT, 2, '0') || '@fschool.edu.vn',
    c_hn
  ) RETURNING id INTO cid;
  t_ids := array_append(t_ids, cid);
END LOOP;

-- ================================================================
-- 8. LỚP HỌC (6 lớp năm học 2025-2026, GVCN = t_ids[1..6])
-- ================================================================
FOR i IN 1..6 LOOP
  INSERT INTO classrooms (name, grade_level, campus_id, homeroom_teacher_id, academic_year_id)
  VALUES (cl_names[i], cl_grades[i], c_hn, t_ids[i], ay1)
  RETURNING id INTO cid;
  cl_ids := array_append(cl_ids, cid);
END LOOP;

-- ================================================================
-- 9. HỌC SINH (60 em, 10 em/lớp)
-- username: student001..student060
-- student_code: FS2025001..FS2025060
-- password: Password@1
-- ================================================================
FOR i IN 1..60 LOOP
  stu_full_name :=
    last_names[((i - 1) % 10) + 1] || ' ' ||
    first_names[((i - 1) % 20) + 1];

  INSERT INTO users (username, password_hash, role)
  VALUES (
    'student' || LPAD(i::TEXT, 3, '0'),
    crypt('Password@1', gen_salt('bf', 10)),
    'STUDENT'
  ) RETURNING id INTO uid;

  INSERT INTO students (
    user_id, student_code, full_name,
    date_of_birth, gender, phone, email,
    classroom_id, campus_id
  ) VALUES (
    uid,
    'FS2025' || LPAD(i::TEXT, 3, '0'),
    stu_full_name,
    DATE '2007-01-01' + ((i * 13) % 365),
    CASE WHEN i % 2 = 0 THEN 'Nam' ELSE 'Nữ' END,
    '09' || LPAD((80000000 + i)::TEXT, 8, '0'),
    'student' || LPAD(i::TEXT, 3, '0') || '@student.fschool.edu.vn',
    cl_ids[((i - 1) / 10) + 1],
    c_hn
  ) RETURNING id INTO cid;
  stu_ids := array_append(stu_ids, cid);
END LOOP;

-- ================================================================
-- 10. PHỤ HUYNH (30 người, mỗi người 2 con liên tiếp)
-- parent01 → student001, student002
-- parent02 → student003, student004  …
-- username: parent01..parent30, password: Password@1
-- ================================================================
FOR i IN 1..30 LOOP
  par_full_name :=
    last_names[((i - 1) % 10) + 1] || ' ' ||
    CASE WHEN i % 2 = 0 THEN 'Văn ' ELSE 'Thị ' END ||
    first_names[((i + 9) % 20) + 1];

  INSERT INTO users (username, password_hash, role)
  VALUES (
    'parent' || LPAD(i::TEXT, 2, '0'),
    crypt('Password@1', gen_salt('bf', 10)),
    'PARENT'
  ) RETURNING id INTO par_uid;

  INSERT INTO parents (
    user_id, parent_code, full_name,
    date_of_birth, gender, phone, email
  ) VALUES (
    par_uid,
    'PH2025' || LPAD(i::TEXT, 3, '0'),
    par_full_name,
    DATE '1975-01-01' + ((i * 17) % 3650),
    CASE WHEN i % 2 = 0 THEN 'Nam' ELSE 'Nữ' END,
    '09' || LPAD((70000000 + i)::TEXT, 8, '0'),
    'parent' || LPAD(i::TEXT, 2, '0') || '@gmail.com'
  ) RETURNING id INTO par_id;

  INSERT INTO parent_students (parent_id, student_id)
  VALUES (par_id, stu_ids[(i * 2) - 1]);
  INSERT INTO parent_students (parent_id, student_id)
  VALUES (par_id, stu_ids[i * 2]);
END LOOP;

-- ================================================================
-- 11. CLASSROOM_SUBJECTS
-- 6 lớp × 10 môn × 2 kỳ = 120 bản ghi
-- Phân công GV: sub j → t_ids[j] (j=1..10)
-- ================================================================
FOR i IN 1..6 LOOP        -- lớp
  FOR j IN 1..10 LOOP    -- môn
    INSERT INTO classroom_subjects (classroom_id, subject_id, teacher_id, semester_id)
    VALUES (cl_ids[i], sub_ids[j], t_ids[j], sem1)
    RETURNING id INTO csid;
    cs_hk1 := array_append(cs_hk1, csid);

    INSERT INTO classroom_subjects (classroom_id, subject_id, teacher_id, semester_id)
    VALUES (cl_ids[i], sub_ids[j], t_ids[j], sem2)
    RETURNING id INTO csid;
    cs_hk2 := array_append(cs_hk2, csid);
  END LOOP;
END LOOP;

-- ================================================================
-- 12. TIẾT HỌC + ĐIỂM DANH
--
-- Lịch TKB trong tuần (mỗi môn 1 tiết/tuần × 2 slot liên tiếp):
--   sub_wc  1-3  → Thứ 2,  tiết 1/3/5
--   sub_wc  4-6  → Thứ 3,  tiết 1/3/5
--   sub_wc  7-9  → Thứ 4,  tiết 1/3/5
--   sub_wc  10   → Thứ 5,  tiết 1
-- Mỗi cs có 15 tiết HK I (bắt đầu 2025-09-01 = Thứ 2)
--          và 10 tiết HK II (bắt đầu 2026-01-05 = Thứ 2)
--
-- Học sinh có s ∈ {3,8} trong lớp: nghỉ nhiều hơn (kích hoạt cảnh báo)
-- ================================================================

-- HK I
FOR i IN 1..60 LOOP
  sub_wc    := ((i - 1) % 10) + 1;
  dow       := (sub_wc - 1) / 3;
  slot_s    := (((sub_wc - 1) % 3) * 2 + 1)::SMALLINT;
  slot_e    := (slot_s + 1)::SMALLINT;
  cl_offset := ((i - 1) / 10) * 10;

  FOR k IN 0..14 LOOP   -- 15 tiết
    lesson_date := DATE '2025-09-01' + dow + (k * 7);
    rid := room_ids[((i - 1 + k) % 10) + 1];

    INSERT INTO lessons (
      classroom_subject_id, lesson_date,
      start_slot_id, end_slot_id, room_id,
      status, has_materials
    ) VALUES (
      cs_hk1[i], lesson_date, slot_s, slot_e, rid,
      'completed',
      (k % 3 = 0)
    ) RETURNING id INTO lid;

    -- Điểm danh 10 học sinh trong lớp
    FOR s IN 1..10 LOOP
      att_status := CASE
        WHEN s IN (3, 8) AND k % 3 = 0          THEN 'unexcused_absent'
        WHEN s IN (3, 8) AND k % 5 = 0          THEN 'excused_absent'
        WHEN s = 5       AND k % 7 = 0          THEN 'late'
        WHEN ((i + k * 3 + s * 7) % 15) = 0    THEN 'late'
        WHEN ((i + k * 3 + s * 7) % 25) = 0    THEN 'excused_absent'
        ELSE 'present'
      END;

      INSERT INTO attendance_records (
        student_id, lesson_id, status,
        recorded_by, recorded_at
      ) VALUES (
        stu_ids[cl_offset + s],
        lid,
        att_status,
        t_user_ids[((i - 1) % 10) + 1],
        lesson_date::TIMESTAMP + INTERVAL '10 hours'
      );
    END LOOP;
  END LOOP;
END LOOP;

-- HK II
FOR i IN 1..60 LOOP
  sub_wc    := ((i - 1) % 10) + 1;
  dow       := (sub_wc - 1) / 3;
  slot_s    := (((sub_wc - 1) % 3) * 2 + 1)::SMALLINT;
  slot_e    := (slot_s + 1)::SMALLINT;
  cl_offset := ((i - 1) / 10) * 10;

  FOR k IN 0..9 LOOP    -- 10 tiết
    lesson_date := DATE '2026-01-05' + dow + (k * 7);
    rid := room_ids[((i - 1 + k) % 10) + 1];

    INSERT INTO lessons (
      classroom_subject_id, lesson_date,
      start_slot_id, end_slot_id, room_id,
      status, has_materials
    ) VALUES (
      cs_hk2[i], lesson_date, slot_s, slot_e, rid,
      CASE WHEN lesson_date < CURRENT_DATE THEN 'completed' ELSE 'scheduled' END,
      (k % 4 = 0)
    ) RETURNING id INTO lid;

    IF lesson_date < CURRENT_DATE THEN
      FOR s IN 1..10 LOOP
        att_status := CASE
          WHEN s IN (3, 8) AND k % 3 = 0          THEN 'unexcused_absent'
          WHEN s IN (3, 8) AND k % 5 = 0          THEN 'excused_absent'
          WHEN s = 5       AND k % 7 = 0          THEN 'late'
          WHEN ((i + k * 5 + s * 11) % 15) = 0   THEN 'late'
          WHEN ((i + k * 5 + s * 11) % 28) = 0   THEN 'excused_absent'
          ELSE 'present'
        END;

        INSERT INTO attendance_records (
          student_id, lesson_id, status,
          recorded_by, recorded_at
        ) VALUES (
          stu_ids[cl_offset + s],
          lid,
          att_status,
          t_user_ids[((i - 1) % 10) + 1],
          lesson_date::TIMESTAMP + INTERVAL '10 hours'
        );
      END LOOP;
    END IF;
  END LOOP;
END LOOP;

-- ================================================================
-- 13. ĐIỂM SỐ
-- HK I: đủ 5 cột (TX1 TX2 TX3 DGKK DCK)
-- HK II: 3 cột (TX1 TX2 DGKK) — chưa có điểm cuối kỳ
-- Điểm: 5.0 – 9.9
-- ================================================================

-- HK I (hoàn thành)
FOR i IN 1..60 LOOP
  cl_offset := ((i - 1) / 10) * 10;
  FOR s IN 1..10 LOOP
    FOR k IN 1..5 LOOP
      INSERT INTO grade_records (
        student_id, classroom_subject_id, component_id,
        score, recorded_by, recorded_at
      ) VALUES (
        stu_ids[cl_offset + s],
        cs_hk1[i],
        k,
        ROUND(CAST(5.0 + ((i * 3 + s * 7 + k * 11) % 50) / 10.0 AS NUMERIC), 1),
        t_user_ids[((i - 1) % 10) + 1],
        '2025-12-15 09:00:00'
      );
    END LOOP;
  END LOOP;
END LOOP;

-- HK II (TX1, TX2, DGKK đã có; DCK chưa)
FOR i IN 1..60 LOOP
  cl_offset := ((i - 1) / 10) * 10;
  FOR s IN 1..10 LOOP
    FOR k IN 1..3 LOOP   -- chỉ TX1(1), TX2(2), DGKK(4) → dùng component_id 1,2,4
      INSERT INTO grade_records (
        student_id, classroom_subject_id, component_id,
        score, recorded_by, recorded_at
      ) VALUES (
        stu_ids[cl_offset + s],
        cs_hk2[i],
        CASE k WHEN 1 THEN 1 WHEN 2 THEN 2 ELSE 4 END,
        ROUND(CAST(5.5 + ((i * 5 + s * 9 + k * 13) % 45) / 10.0 AS NUMERIC), 1),
        t_user_ids[((i - 1) % 10) + 1],
        '2026-04-10 09:00:00'
      );
    END LOOP;
  END LOOP;
END LOOP;

-- ================================================================
-- 14. THÔNG BÁO
-- ================================================================

-- Sự kiện (event) → toàn trường
INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Lịch nghỉ Tết Nguyên Đán 2026',
  'Nhà trường thông báo lịch nghỉ Tết Nguyên Đán Bính Ngọ từ ngày 26/01/2026 đến 03/02/2026. Học sinh quay lại trường vào ngày 04/02/2026.',
  'event', 'all', NULL, admin_uid, '2026-01-10'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Hội thao truyền thống 2026',
  'Nhà trường tổ chức Hội thao truyền thống vào ngày 20/03/2026. Các lớp đăng ký tham gia trước ngày 05/03/2026. Có các môn: Bóng đá, Cầu lông, Kéo co, Nhảy dây.',
  'event', 'all', NULL, admin_uid, '2026-02-20'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Họp phụ huynh tổng kết HK I năm học 2025-2026',
  'Nhà trường tổ chức họp phụ huynh tổng kết học kỳ I vào ngày 22/12/2025 (thứ Hai). Đề nghị phụ huynh sắp xếp thời gian tham dự. Chi tiết lịch từng lớp xem tại bảng thông báo trường.',
  'event', 'all', NULL, admin_uid, '2025-12-10'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Tuần lễ hướng nghiệp 2026 dành cho học sinh lớp 12',
  'Chương trình Tuần lễ hướng nghiệp dành cho học sinh lớp 12 sẽ diễn ra từ ngày 09-13/03/2026. Học sinh đăng ký tham gia tại văn phòng nhà trường trước 28/02/2026.',
  'event', 'all', NULL, admin_uid, '2026-02-15'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Thi thử THPT Quốc gia đợt 1 — dành cho lớp 12',
  'Nhà trường tổ chức kỳ thi thử THPT Quốc gia đợt 1 vào ngày 25-26/02/2026. Lịch chi tiết theo môn sẽ được phát vào đầu tháng 2.',
  'event', 'all', NULL, admin_uid, '2026-01-25'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Khai giảng năm học 2025-2026',
  'Trân trọng thông báo: Lễ Khai giảng năm học 2025-2026 sẽ được tổ chức vào 07:30 ngày 05/09/2025 (thứ Sáu). Học sinh mặc đồng phục, có mặt trước 07:00.',
  'event', 'all', NULL, admin_uid, '2025-08-28'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

-- GVCN (homeroom)
INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  '[10A1] Nhắc nhở đồng phục HK II',
  'Học sinh lớp 10A1 lưu ý mặc đúng đồng phục quy định khi đến trường trong học kỳ II. Tuần sau ban giám thị sẽ kiểm tra toàn trường.',
  'homeroom', 'classroom', cl_ids[1], t_user_ids[1], '2026-01-06'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, s.user_id FROM students s WHERE s.classroom_id = cl_ids[1] AND s.user_id IS NOT NULL;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT DISTINCT n_id, p.user_id
FROM parent_students ps
JOIN students st ON ps.student_id = st.id AND st.classroom_id = cl_ids[1]
JOIN parents p ON ps.parent_id = p.id
WHERE p.user_id IS NOT NULL;

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  '[11B1] Lịch tiết học bù môn Toán',
  'Lớp 11B1 sẽ có tiết học bù môn Toán vào thứ Bảy ngày 24/01/2026 (tiết 1-2, phòng A1.03). Học sinh chú ý sắp xếp lịch.',
  'homeroom', 'classroom', cl_ids[3], t_user_ids[3], '2026-01-15'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, s.user_id FROM students s WHERE s.classroom_id = cl_ids[3] AND s.user_id IS NOT NULL;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT DISTINCT n_id, p.user_id
FROM parent_students ps
JOIN students st ON ps.student_id = st.id AND st.classroom_id = cl_ids[3]
JOIN parents p ON ps.parent_id = p.id
WHERE p.user_id IS NOT NULL;

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  '[12C1] Tổng kết HK I — lớp đạt 90% học lực Khá/Giỏi',
  'Kính gửi phụ huynh và học sinh lớp 12C1. Kết quả HK I: 9/10 em đạt học lực Giỏi, 1 em Khá. Lớp xếp hạng 1 khối 12. Đề nghị học sinh tiếp tục phát huy trong HK II.',
  'homeroom', 'classroom', cl_ids[5], t_user_ids[5], '2026-01-03'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, s.user_id FROM students s WHERE s.classroom_id = cl_ids[5] AND s.user_id IS NOT NULL;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT DISTINCT n_id, p.user_id
FROM parent_students ps
JOIN students st ON ps.student_id = st.id AND st.classroom_id = cl_ids[5]
JOIN parents p ON ps.parent_id = p.id
WHERE p.user_id IS NOT NULL;

-- Điểm danh (attendance)
INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Cảnh báo: Số buổi nghỉ vượt ngưỡng quy định',
  'Học sinh đã nghỉ học không phép 4 buổi trong học kỳ này, vượt ngưỡng cảnh báo. Đề nghị phụ huynh phối hợp với nhà trường để theo dõi và nhắc nhở học sinh.',
  'attendance', 'individual', stu_ids[3], admin_uid, '2026-03-10'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, s.user_id FROM students s WHERE s.id = stu_ids[3] AND s.user_id IS NOT NULL;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT DISTINCT n_id, p.user_id
FROM parent_students ps
JOIN parents p ON ps.parent_id = p.id
WHERE ps.student_id = stu_ids[3] AND p.user_id IS NOT NULL;

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Vắng học không phép ngày 05/03/2026',
  'Học sinh vắng mặt không có lý do trong buổi học ngày 05/03/2026. Đề nghị phụ huynh liên hệ giáo viên chủ nhiệm để giải trình.',
  'attendance', 'individual', stu_ids[18], t_user_ids[2], '2026-03-05'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, s.user_id FROM students s WHERE s.id = stu_ids[18] AND s.user_id IS NOT NULL;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT DISTINCT n_id, p.user_id
FROM parent_students ps
JOIN parents p ON ps.parent_id = p.id
WHERE ps.student_id = stu_ids[18] AND p.user_id IS NOT NULL;

-- Điểm (grade)
INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Kết quả thi HK I 2025-2026 đã cập nhật',
  'Điểm thi học kỳ I năm học 2025-2026 đã được nhập đầy đủ. Học sinh và phụ huynh có thể xem bảng điểm chi tiết trên ứng dụng MyFPTSchool.',
  'grade', 'all', NULL, admin_uid, '2026-01-05'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Điểm thường xuyên tháng 10/2025 đã cập nhật',
  'Giáo viên đã nhập điểm thường xuyên tháng 10/2025. Học sinh kiểm tra và phản hồi nếu có sai sót trước ngày 05/11/2025.',
  'grade', 'all', NULL, admin_uid, '2025-10-31'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Điểm giữa kỳ II đã được nhập',
  'Điểm đánh giá giữa kỳ II (DGKK) đã được nhập xong. Học sinh vào ứng dụng kiểm tra. Điểm cuối kỳ sẽ được nhập sau kỳ thi tháng 5.',
  'grade', 'all', NULL, admin_uid, '2026-04-10'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

-- Học tập (study)
INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Tài liệu ôn thi HK I đã được đăng tải',
  'Giáo viên đã đăng tải đề cương và tài liệu ôn tập HK I trên hệ thống. Học sinh truy cập mục Tài liệu trên ứng dụng để tải về.',
  'study', 'all', NULL, admin_uid, '2025-12-01'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Lịch kiểm tra định kỳ tháng 3/2026',
  'Lịch kiểm tra 1 tiết tháng 3 đã được lên kế hoạch: Toán (02/03), Văn (04/03), Anh (06/03), Lý (09/03), Hóa (11/03). Học sinh chú ý ôn tập.',
  'study', 'all', NULL, admin_uid, '2026-02-25'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

INSERT INTO notifications (title, body, category, target_type, target_id, created_by, created_at)
VALUES (
  'Hội thi Olympic Toán và Tin học cấp trường 2026',
  'Nhà trường tổ chức Hội thi Olympic Toán và Tin học cấp trường vào ngày 14-15/04/2026. Học sinh đăng ký qua GVCN trước 31/03/2026. Giải thưởng hấp dẫn!',
  'study', 'all', NULL, admin_uid, '2026-03-20'
) RETURNING id INTO n_id;
INSERT INTO notification_recipients (notification_id, user_id)
SELECT n_id, u.id FROM users u WHERE u.role IN ('STUDENT','PARENT');

RAISE NOTICE 'V8: Seed data đã được chèn thành công.';

END $seed$;
