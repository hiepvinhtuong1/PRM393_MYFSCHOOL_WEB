package vn.edu.fpt.myfptschool.grade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.grade.dto.GradeSubjectResponse;
import vn.edu.fpt.myfptschool.grade.entity.GradeRecord;
import vn.edu.fpt.myfptschool.grade.repository.GradeRecordRepository;
import vn.edu.fpt.myfptschool.parent.entity.Parent;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final GradeRecordRepository gradeRecordRepository;

    @Transactional(readOnly = true)
    public List<GradeSubjectResponse> getGrades(String username, Long semesterId, Long studentId) {
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

        List<GradeRecord> records = gradeRecordRepository.findByStudentAndSemester(student, semesterId);

        Map<Long, List<GradeRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(gr -> gr.getClassroomSubject().getId()));

        return grouped.entrySet().stream()
                .map(e -> {
                    List<GradeRecord> grList = e.getValue();
                    var cs = grList.get(0).getClassroomSubject();
                    return GradeSubjectResponse.from(
                            cs.getId(),
                            cs.getSubject().getName(),
                            cs.getSubject().getCoefficient(),
                            grList
                    );
                })
                .sorted((a, b) -> a.subjectName().compareTo(b.subjectName()))
                .toList();
    }
}
