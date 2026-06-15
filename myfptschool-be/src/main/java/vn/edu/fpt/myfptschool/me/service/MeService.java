package vn.edu.fpt.myfptschool.me.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.repository.ClassroomSubjectRepository;
import vn.edu.fpt.myfptschool.academic.repository.SemesterRepository;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.me.dto.ParentProfileResponse;
import vn.edu.fpt.myfptschool.me.dto.SemesterResponse;
import vn.edu.fpt.myfptschool.me.dto.StudentProfileResponse;
import vn.edu.fpt.myfptschool.me.dto.TeacherContactResponse;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;
import vn.edu.fpt.myfptschool.teacher.entity.Teacher;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final SemesterRepository semesterRepository;
    private final ClassroomSubjectRepository classroomSubjectRepository;

    @Transactional(readOnly = true)
    public Object getProfile(String username) {
        log.info("[MeService] getProfile username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("[MeService] user found id={}, role={}", user.getId(), user.getRole());

        return switch (user.getRole()) {
            case STUDENT -> studentRepository.findByUserWithClassroom(user)
                    .map(StudentProfileResponse::from)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            case PARENT -> parentRepository.findByUserWithChildren(user)
                    .map(ParentProfileResponse::from)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            default -> throw new AppException(ErrorCode.NOT_FOUND);
        };
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getSemesters() {
        return semesterRepository.findAllWithAcademicYear().stream()
                .map(SemesterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherContactResponse> getTeachers(String username, Long studentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Student student = switch (user.getRole()) {
            case STUDENT -> studentRepository.findByUserWithClassroom(user)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            case PARENT -> {
                if (studentId == null) throw new AppException(ErrorCode.NOT_FOUND);
                yield studentRepository.findByIdWithClassroom(studentId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            }
            default -> throw new AppException(ErrorCode.NOT_FOUND);
        };

        if (student.getClassroom() == null) return List.of();

        List<TeacherContactResponse> result = new ArrayList<>();

        // Homeroom teacher (nullable)
        Teacher homeroom = student.getClassroom().getHomeroomTeacher();
        if (homeroom != null) {
            result.add(TeacherContactResponse.ofHomeroom(homeroom));
        }

        // Subject teachers from the latest semester
        var allSubjects = classroomSubjectRepository
                .findAllByClassroomId(student.getClassroom().getId());

        if (!allSubjects.isEmpty()) {
            var latestSemesterId = allSubjects.stream()
                    .max(Comparator.comparing(cs -> cs.getSemester().getStartDate()))
                    .map(cs -> cs.getSemester().getId())
                    .orElse(null);

            Set<Long> seenIds = new HashSet<>();
            allSubjects.stream()
                    .filter(cs -> cs.getSemester().getId().equals(latestSemesterId))
                    .filter(cs -> seenIds.add(cs.getTeacher().getId()))
                    .map(cs -> TeacherContactResponse.ofSubject(cs.getTeacher(), cs.getSubject().getName()))
                    .forEach(result::add);
        }

        return result;
    }
}
