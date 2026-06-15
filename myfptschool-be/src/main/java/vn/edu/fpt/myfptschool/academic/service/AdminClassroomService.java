package vn.edu.fpt.myfptschool.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.dto.CampusResponse;
import vn.edu.fpt.myfptschool.academic.dto.ClassroomResponse;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.academic.repository.CampusRepository;
import vn.edu.fpt.myfptschool.academic.repository.ClassroomRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.student.dto.StudentSummaryResponse;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final CampusRepository campusRepository;

    @Transactional(readOnly = true)
    public List<CampusResponse> getAllCampuses() {
        return campusRepository.findAll().stream().map(CampusResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ClassroomResponse> getAllClassrooms() {
        return classroomRepository.findAllWithDetails().stream()
                .map(cl -> ClassroomResponse.from(cl, studentRepository.countByClassroom(cl)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<StudentSummaryResponse> getStudentsByClassroom(Long classroomId, int page, int size) {
        Classroom classroom = classroomRepository.findByIdWithDetails(classroomId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return studentRepository
                .findByClassroomOrderByFullName(classroom, PageRequest.of(page, size))
                .map(StudentSummaryResponse::from);
    }
}
