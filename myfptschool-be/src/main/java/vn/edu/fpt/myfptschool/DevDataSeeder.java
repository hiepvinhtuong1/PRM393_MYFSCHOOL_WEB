package vn.edu.fpt.myfptschool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.entity.AcademicYear;
import vn.edu.fpt.myfptschool.academic.entity.Campus;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.academic.repository.AcademicYearRepository;
import vn.edu.fpt.myfptschool.academic.repository.CampusRepository;
import vn.edu.fpt.myfptschool.academic.repository.ClassroomRepository;
import vn.edu.fpt.myfptschool.auth.entity.Role;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.parent.entity.Parent;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

import java.time.LocalDate;

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

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // Users
        User admin   = userRepository.save(User.create("admin001",   passwordEncoder.encode("admin123"),   Role.ADMIN));
        User student = userRepository.save(User.create("student001", passwordEncoder.encode("student123"), Role.STUDENT));
        User parent  = userRepository.save(User.create("parent001",  passwordEncoder.encode("parent123"),  Role.PARENT));
        User teacher = userRepository.save(User.create("teacher001", passwordEncoder.encode("teacher123"), Role.TEACHER));

        // Academic year, Campus + Classroom
        AcademicYear academicYear = academicYearRepository.save(
                AcademicYear.create("2025-2026", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 6, 30)));
        Campus campus = campusRepository.save(Campus.create("FPT Hà Nội", "Khu Công nghệ cao Hòa Lạc, Thạch Thất, Hà Nội"));
        Classroom classroom = classroomRepository.save(Classroom.create("SE1801", (short) 12, campus, academicYear));

        // Student profile
        Student studentProfile = studentRepository.save(Student.create(
                student, "STU001", "Nguyễn Văn An",
                LocalDate.of(2005, 3, 15), "Nam", "0901234567", "an.nguyen@fpt.edu.vn",
                classroom
        ));

        // Parent profile
        Parent parentProfile = parentRepository.save(Parent.create(
                parent, "PAR001", "Nguyễn Văn Bình",
                LocalDate.of(1975, 8, 20), "Nam", "0912345678", "binh.nguyen@gmail.com"
        ));
        parentProfile.addChild(studentProfile);
        parentRepository.save(parentProfile);

        log.info("Dev seed: users, campus, classroom, student, parent created");
    }
}
