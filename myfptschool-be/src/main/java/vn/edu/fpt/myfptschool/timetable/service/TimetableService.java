package vn.edu.fpt.myfptschool.timetable.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.parent.entity.Parent;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;
import vn.edu.fpt.myfptschool.timetable.dto.LessonResponse;
import vn.edu.fpt.myfptschool.timetable.repository.LessonRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public List<LessonResponse> getTimetable(String username, LocalDate date, Long studentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Classroom classroom = switch (user.getRole()) {
            case STUDENT -> {
                Student student = studentRepository.findByUserWithClassroom(user)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                yield student.getClassroom();
            }
            case PARENT -> {
                Parent parent = parentRepository.findByUserWithChildren(user)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                Student child = (studentId != null)
                        ? parent.getChildren().stream()
                                .filter(s -> s.getId().equals(studentId))
                                .findFirst()
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
                        : parent.getChildren().stream()
                                .findFirst()
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                yield child.getClassroom();
            }
            default -> throw new AppException(ErrorCode.FORBIDDEN);
        };

        return lessonRepository.findByClassroomAndDate(classroom, date)
                .stream()
                .map(LessonResponse::from)
                .toList();
    }
}
