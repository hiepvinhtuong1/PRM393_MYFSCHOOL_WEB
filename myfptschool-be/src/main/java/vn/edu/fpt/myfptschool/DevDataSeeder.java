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

        log.info("Dev seed: all data created (users, academic structure, timetable, grades)");
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
