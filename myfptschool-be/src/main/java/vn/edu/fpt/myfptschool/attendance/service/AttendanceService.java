package vn.edu.fpt.myfptschool.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;
import vn.edu.fpt.myfptschool.attendance.dto.AttendanceSubjectResponse;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceRecord;
import vn.edu.fpt.myfptschool.attendance.repository.AttendanceRecordRepository;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.parent.entity.Parent;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;
import vn.edu.fpt.myfptschool.timetable.repository.LessonRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public List<AttendanceSubjectResponse> getAttendance(String username, Long semesterId, Long studentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Student student = switch (user.getRole()) {
            case STUDENT -> studentRepository.findByUserWithClassroom(user)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            case PARENT -> {
                Parent parent = parentRepository.findByUserWithChildren(user)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                yield (studentId != null)
                        ? parent.getChildren().stream()
                                .filter(s -> s.getId().equals(studentId))
                                .findFirst()
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
                        : parent.getChildren().stream()
                                .findFirst()
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            }
            default -> throw new AppException(ErrorCode.FORBIDDEN);
        };

        List<AttendanceRecord> records =
                attendanceRecordRepository.findByStudentAndSemester(student, semesterId);

        Map<Long, List<AttendanceRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(ar -> ar.getLesson().getClassroomSubject().getId()));

        List<Object[]> lessonCounts = lessonRepository.countLessonsPerClassroomSubject(
                student.getClassroom().getId(), semesterId);

        Map<Long, Integer> totalSessionsMap = lessonCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        return grouped.entrySet().stream()
                .map(e -> {
                    List<AttendanceRecord> arList = e.getValue();
                    ClassroomSubject cs = arList.get(0).getLesson().getClassroomSubject();
                    int total = totalSessionsMap.getOrDefault(cs.getId(), 0);
                    return AttendanceSubjectResponse.from(cs, total, arList);
                })
                .sorted((a, b) -> a.subjectName().compareTo(b.subjectName()))
                .toList();
    }
}
