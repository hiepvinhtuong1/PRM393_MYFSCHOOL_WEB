package vn.edu.fpt.myfptschool.teacher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.entity.Campus;
import vn.edu.fpt.myfptschool.academic.repository.CampusRepository;
import vn.edu.fpt.myfptschool.auth.entity.Role;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.teacher.dto.*;
import vn.edu.fpt.myfptschool.teacher.entity.Teacher;
import vn.edu.fpt.myfptschool.teacher.repository.TeacherRepository;

@Service
@RequiredArgsConstructor
public class AdminTeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final CampusRepository campusRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public TeacherPageResponse getAllTeachers(int page, int size) {
        Page<Teacher> result = teacherRepository.findAll(PageRequest.of(page, size));
        return new TeacherPageResponse(
                result.getContent().stream().map(TeacherResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Tên đăng nhập đã tồn tại: " + req.username());
        }

        Campus campus = campusRepository.findById(req.campusId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        String password = (req.password() != null && !req.password().isBlank())
                ? req.password()
                : "Teacher@123";

        User user = userRepository.save(
                User.create(req.username(), passwordEncoder.encode(password), Role.TEACHER));

        Teacher teacher = teacherRepository.save(
                Teacher.create(user, req.fullName(), req.phone(), req.email(), campus));

        return TeacherResponse.from(teacher);
    }

    @Transactional
    public TeacherResponse updateTeacher(Long id, UpdateTeacherRequest req) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Campus campus = campusRepository.findById(req.campusId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        teacher.update(req.fullName(), req.phone(), req.email(), campus);

        return TeacherResponse.from(teacherRepository.save(teacher));
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacher(Long id) {
        return TeacherResponse.from(
                teacherRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        );
    }
}
