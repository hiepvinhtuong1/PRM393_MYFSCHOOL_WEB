package vn.edu.fpt.myfptschool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.entity.*;
import vn.edu.fpt.myfptschool.academic.repository.*;
import vn.edu.fpt.myfptschool.auth.entity.Role;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.parent.entity.Parent;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;
import vn.edu.fpt.myfptschool.teacher.entity.Teacher;
import vn.edu.fpt.myfptschool.teacher.repository.TeacherRepository;
import vn.edu.fpt.myfptschool.timetable.entity.Lesson;
import vn.edu.fpt.myfptschool.timetable.entity.Room;
import vn.edu.fpt.myfptschool.timetable.entity.TimeSlot;
import vn.edu.fpt.myfptschool.timetable.repository.LessonRepository;
import vn.edu.fpt.myfptschool.timetable.repository.RoomRepository;
import vn.edu.fpt.myfptschool.timetable.repository.TimeSlotRepository;
import vn.edu.fpt.myfptschool.grade.entity.GradeRecord;
import vn.edu.fpt.myfptschool.grade.entity.ScoreComponent;
import vn.edu.fpt.myfptschool.grade.repository.GradeRecordRepository;
import vn.edu.fpt.myfptschool.grade.repository.ScoreComponentRepository;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceRecord;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceStatus;
import vn.edu.fpt.myfptschool.attendance.repository.AttendanceRecordRepository;
import vn.edu.fpt.myfptschool.notification.entity.Notification;
import vn.edu.fpt.myfptschool.notification.entity.NotificationCategory;
import vn.edu.fpt.myfptschool.notification.entity.NotificationRecipient;
import vn.edu.fpt.myfptschool.notification.entity.NotificationTargetType;
import vn.edu.fpt.myfptschool.notification.repository.NotificationRecipientRepository;
import vn.edu.fpt.myfptschool.notification.repository.NotificationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AcademicYearRepository academicYearRepository;
    private final CampusRepository campusRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final TeacherRepository teacherRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomSubjectRepository classroomSubjectRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final RoomRepository roomRepository;
    private final LessonRepository lessonRepository;
    private final ScoreComponentRepository scoreComponentRepository;
    private final GradeRecordRepository gradeRecordRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // --- Users ---
        User admin   = userRepository.save(User.create("admin001",   passwordEncoder.encode("admin123"),   Role.ADMIN));
        User student = userRepository.save(User.create("student001", passwordEncoder.encode("student123"), Role.STUDENT));
        User parent  = userRepository.save(User.create("parent001",  passwordEncoder.encode("parent123"),  Role.PARENT));
        User teacher = userRepository.save(User.create("teacher001", passwordEncoder.encode("teacher123"), Role.TEACHER));

        // --- Academic structure ---
        AcademicYear academicYear = academicYearRepository.save(
                AcademicYear.create("2025-2026", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 6, 30)));
        Campus campus = campusRepository.save(
                Campus.create("FPT Hà Nội", "Khu Công nghệ cao Hòa Lạc, Thạch Thất, Hà Nội"));
        Classroom classroom = classroomRepository.save(
                Classroom.create("SE1801", (short) 12, campus, academicYear));
        Semester semester = semesterRepository.save(
                Semester.create(academicYear, "HK II", LocalDate.of(2026, 1, 6), LocalDate.of(2026, 6, 30)));

        // --- Student & Parent profiles ---
        Student studentProfile = studentRepository.save(Student.create(
                student, "STU001", "Nguyễn Văn An",
                LocalDate.of(2005, 3, 15), "Nam", "0901234567", "an.nguyen@fpt.edu.vn", classroom));
        Parent parentProfile = parentRepository.save(Parent.create(
                parent, "PAR001", "Nguyễn Văn Bình",
                LocalDate.of(1975, 8, 20), "Nam", "0912345678", "binh.nguyen@gmail.com"));
        parentProfile.addChild(studentProfile);
        parentRepository.save(parentProfile);

        // --- Teacher ---
        Teacher teacherProfile = teacherRepository.save(
                Teacher.create(teacher, "Nguyễn Văn A", "a.nguyen@fpt.edu.vn", campus));

        // --- Subjects ---
        Subject toan   = subjectRepository.save(Subject.create("Toán",       "#F97316", 2));
        Subject van    = subjectRepository.save(Subject.create("Ngữ Văn",    "#3B82F6", 2));
        Subject anh    = subjectRepository.save(Subject.create("Tiếng Anh",  "#3B82F6", 1));
        Subject vatly  = subjectRepository.save(Subject.create("Vật Lý",     "#22C55E", 1));

        // --- ClassroomSubjects ---
        ClassroomSubject csToan  = classroomSubjectRepository.save(ClassroomSubject.create(classroom, toan,  teacherProfile, semester));
        ClassroomSubject csVan   = classroomSubjectRepository.save(ClassroomSubject.create(classroom, van,   teacherProfile, semester));
        ClassroomSubject csAnh   = classroomSubjectRepository.save(ClassroomSubject.create(classroom, anh,   teacherProfile, semester));
        ClassroomSubject csVatly = classroomSubjectRepository.save(ClassroomSubject.create(classroom, vatly, teacherProfile, semester));

        // --- Room ---
        Room room201  = roomRepository.save(Room.create("Phòng 201", campus));
        Room roomLab1 = roomRepository.save(Room.create("Phòng Lab 1", campus));

        // --- TimeSlots (seeded by V2, just look up) ---
        TimeSlot slot1 = slot(1);
        TimeSlot slot2 = slot(2);
        TimeSlot slot3 = slot(3);
        TimeSlot slot4 = slot(4);
        TimeSlot slot5 = slot(5);
        TimeSlot slot7 = slot(7);
        TimeSlot slot8 = slot(8);

        // --- Lessons for test week 2026-06-08 to 2026-06-13 ---
        // Thứ 2 (08/06)
        lessonRepository.saveAll(List.of(
                Lesson.create(csToan,  LocalDate.of(2026, 6, 8), slot1, slot2, room201),
                Lesson.create(csVan,   LocalDate.of(2026, 6, 8), slot3, slot4, room201),
                Lesson.create(csAnh,   LocalDate.of(2026, 6, 8), slot7, slot8, room201),
                // Thứ 3 (09/06)
                Lesson.create(csVatly, LocalDate.of(2026, 6, 9), slot1, slot2, roomLab1),
                Lesson.create(csAnh,   LocalDate.of(2026, 6, 9), slot3, slot4, room201),
                // Thứ 4 (10/06)
                Lesson.create(csToan,  LocalDate.of(2026, 6, 10), slot3, slot4, room201),
                Lesson.create(csVan,   LocalDate.of(2026, 6, 10), slot7, slot8, room201),
                // Thứ 5 (11/06) — hôm nay
                Lesson.create(csVan,   LocalDate.of(2026, 6, 11), slot1, slot2, room201),
                Lesson.create(csAnh,   LocalDate.of(2026, 6, 11), slot3, slot4, room201),
                Lesson.create(csToan,  LocalDate.of(2026, 6, 11), slot5, slot5, room201),
                Lesson.create(csVatly, LocalDate.of(2026, 6, 11), slot7, slot8, roomLab1),
                // Thứ 6 (12/06)
                Lesson.create(csAnh,   LocalDate.of(2026, 6, 12), slot1, slot2, room201),
                Lesson.create(csVan,   LocalDate.of(2026, 6, 12), slot3, slot3, room201),
                Lesson.create(csToan,  LocalDate.of(2026, 6, 12), slot5, slot5, room201)
        ));

        // --- Grade records (HK II 2025-2026, chưa có DCK) ---
        ScoreComponent tx1  = comp("TX1");
        ScoreComponent tx2  = comp("TX2");
        ScoreComponent tx3  = comp("TX3");
        ScoreComponent dgkk = comp("DGKK");

        gradeRecordRepository.saveAll(List.of(
            // Toán
            GradeRecord.create(studentProfile, csToan, tx1,  bd(8.0)),
            GradeRecord.create(studentProfile, csToan, tx2,  bd(7.5)),
            GradeRecord.create(studentProfile, csToan, tx3,  bd(9.0)),
            GradeRecord.create(studentProfile, csToan, dgkk, bd(8.5)),
            // Ngữ Văn
            GradeRecord.create(studentProfile, csVan, tx1,  bd(7.0)),
            GradeRecord.create(studentProfile, csVan, tx2,  bd(7.5)),
            GradeRecord.create(studentProfile, csVan, tx3,  bd(8.0)),
            GradeRecord.create(studentProfile, csVan, dgkk, bd(7.5)),
            // Tiếng Anh
            GradeRecord.create(studentProfile, csAnh, tx1,  bd(9.0)),
            GradeRecord.create(studentProfile, csAnh, tx2,  bd(8.5)),
            GradeRecord.create(studentProfile, csAnh, tx3,  bd(9.5)),
            GradeRecord.create(studentProfile, csAnh, dgkk, bd(9.0)),
            // Vật Lý
            GradeRecord.create(studentProfile, csVatly, tx1,  bd(6.5)),
            GradeRecord.create(studentProfile, csVatly, tx2,  bd(7.0)),
            GradeRecord.create(studentProfile, csVatly, tx3,  bd(6.0)),
            GradeRecord.create(studentProfile, csVatly, dgkk, bd(6.5))
        ));

        // --- Attendance records ---
        // Query lessons back from DB (JPA auto-flushes before query within same transaction)
        List<Lesson> toanLessons  = lessonRepository.findByClassroomSubject(csToan);
        List<Lesson> vanLessons   = lessonRepository.findByClassroomSubject(csVan);
        List<Lesson> anhLessons   = lessonRepository.findByClassroomSubject(csAnh);
        List<Lesson> vatlyLessons = lessonRepository.findByClassroomSubject(csVatly);

        // Toán: 4 lessons — present, present, excused_absent, late
        seedAttendance(studentProfile, toanLessons, new AttendanceStatus[]{
                AttendanceStatus.present, AttendanceStatus.present,
                AttendanceStatus.excused_absent, AttendanceStatus.late
        });

        // Ngữ Văn: 4 lessons — unexcused_absent, present, present, excused_absent
        seedAttendance(studentProfile, vanLessons, new AttendanceStatus[]{
                AttendanceStatus.unexcused_absent, AttendanceStatus.present,
                AttendanceStatus.present, AttendanceStatus.excused_absent
        });

        // Tiếng Anh: 4 lessons — all present
        seedAttendance(studentProfile, anhLessons, new AttendanceStatus[]{
                AttendanceStatus.present, AttendanceStatus.present,
                AttendanceStatus.present, AttendanceStatus.present
        });

        // Vật Lý: 2 lessons — late, present
        seedAttendance(studentProfile, vatlyLessons, new AttendanceStatus[]{
                AttendanceStatus.late, AttendanceStatus.present
        });

        // --- Notifications ---
        Notification n1 = notificationRepository.save(Notification.create(
                "Cảnh báo nghỉ học",
                "Học sinh Nguyễn Văn An đã vắng mặt không phép môn Ngữ Văn. Đề nghị phụ huynh liên hệ giáo viên chủ nhiệm.",
                NotificationCategory.attendance, NotificationTargetType.individual, studentProfile.getId(), admin));
        Notification n2 = notificationRepository.save(Notification.create(
                "Điểm kiểm tra giữa kỳ đã được cập nhật",
                "Điểm ĐGKK học kỳ II đã được nhập đầy đủ. Vui lòng kiểm tra trên ứng dụng.",
                NotificationCategory.grade, NotificationTargetType.classroom, classroom.getId(), admin));
        Notification n3 = notificationRepository.save(Notification.create(
                "Thông báo lịch thi học kỳ II",
                "Lịch thi học kỳ II năm học 2025-2026: Toán ngày 25/06, Ngữ Văn ngày 26/06, Tiếng Anh ngày 27/06.",
                NotificationCategory.study, NotificationTargetType.all, null, admin));
        Notification n4 = notificationRepository.save(Notification.create(
                "Họp phụ huynh cuối năm",
                "Trường tổ chức họp phụ huynh tổng kết năm học vào ngày 28/06/2026 lúc 8:00 sáng tại hội trường.",
                NotificationCategory.event, NotificationTargetType.all, null, admin));

        // Send to student and parent
        List<User> recipients = List.of(student, parent);
        for (Notification n : List.of(n1, n2, n3, n4)) {
            for (User u : recipients) {
                notificationRecipientRepository.save(NotificationRecipient.create(n, u));
            }
        }

        log.info("Dev seed: all data created (users, academic structure, timetable, grades, attendance, notifications)");
    }

    private void seedAttendance(Student student, List<Lesson> lessons, AttendanceStatus[] statuses) {
        for (int i = 0; i < lessons.size() && i < statuses.length; i++) {
            attendanceRecordRepository.save(AttendanceRecord.create(student, lessons.get(i), statuses[i]));
        }
    }

    private ScoreComponent comp(String code) {
        return scoreComponentRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("ScoreComponent " + code + " not found — V2 migration not applied?"));
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }

    private TimeSlot slot(int number) {
        return timeSlotRepository.findBySlotNumber((short) number)
                .orElseThrow(() -> new IllegalStateException("TimeSlot " + number + " not found — V2 migration not applied?"));
    }
}
