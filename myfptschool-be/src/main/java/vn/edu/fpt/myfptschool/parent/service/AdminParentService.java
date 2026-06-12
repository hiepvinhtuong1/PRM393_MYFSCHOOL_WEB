package vn.edu.fpt.myfptschool.parent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.auth.entity.Role;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.parent.dto.*;
import vn.edu.fpt.myfptschool.parent.entity.Parent;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class AdminParentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MAX_PARENTS_PER_STUDENT = 2;
    private static final int MIN_PARENT_AGE = 25;

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ParentResponse createParent(CreateParentRequest req) {
        if (parentRepository.existsByParentCode(req.parentCode())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "CCCD/CMND đã được đăng ký: " + req.parentCode());
        }

        // CCCD is all digits — use directly as username; admin may override with custom username
        String username = (req.username() != null && !req.username().isBlank())
                ? req.username()
                : req.parentCode();

        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Tên đăng nhập đã tồn tại: " + username);
        }

        LocalDate dob = parseAndValidateDate(req.dateOfBirth());

        String password = (req.password() != null && !req.password().isBlank())
                ? req.password()
                : "Parent@123";

        User user = userRepository.save(
                User.create(username, passwordEncoder.encode(password), Role.PARENT));

        Parent parent = parentRepository.save(
                Parent.create(user, req.parentCode(), req.fullName(), dob,
                        req.gender(), req.phone(), req.email()));

        return ParentResponse.from(parent);
    }

    @Transactional
    public ParentResponse updateParent(Long id, UpdateParentRequest req) {
        Parent parent = parentRepository.findByIdWithChildren(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        LocalDate dob = parseAndValidateDate(req.dateOfBirth());
        parent.update(req.fullName(), dob, req.gender(), req.phone(), req.email());

        return ParentResponse.from(parentRepository.save(parent));
    }

    @Transactional(readOnly = true)
    public ParentResponse getParent(Long id) {
        return ParentResponse.from(
                parentRepository.findByIdWithChildren(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        );
    }

    @Transactional
    public ParentResponse linkParentToStudent(Long studentId, Long parentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Parent parent = parentRepository.findByIdWithChildren(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (parentRepository.existsByIdAndChildrenId(parentId, studentId)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Phụ huynh đã được liên kết với học sinh này");
        }

        long currentParentCount = parentRepository.countByChildrenId(studentId);
        if (currentParentCount >= MAX_PARENTS_PER_STUDENT) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Học sinh đã có " + MAX_PARENTS_PER_STUDENT + " phụ huynh, không thể thêm");
        }

        parent.addChild(student);
        return ParentResponse.from(parentRepository.save(parent));
    }

    private LocalDate parseAndValidateDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            LocalDate dob = LocalDate.parse(raw, DATE_FMT);
            LocalDate today = LocalDate.now();
            int age = today.getYear() - dob.getYear();
            if (dob.plusYears(age).isAfter(today)) age--;
            if (age < MIN_PARENT_AGE) {
                throw new AppException(ErrorCode.VALIDATION_FAILED,
                        "Phụ huynh phải ít nhất " + MIN_PARENT_AGE + " tuổi (hiện tại " + age + " tuổi)");
            }
            return dob;
        } catch (DateTimeParseException e) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Sai định dạng ngày sinh, dùng dd/MM/yyyy");
        }
    }
}
