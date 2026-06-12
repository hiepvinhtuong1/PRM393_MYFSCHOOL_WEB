package vn.edu.fpt.myfptschool.student.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.academic.repository.ClassroomRepository;
import vn.edu.fpt.myfptschool.auth.entity.Role;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.student.dto.*;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStudentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StudentSummaryResponse createStudent(CreateStudentRequest req) {
        if (studentRepository.existsByStudentCode(req.studentCode())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Mã học sinh đã tồn tại: " + req.studentCode());
        }

        String username = req.username() != null && !req.username().isBlank()
                ? req.username()
                : req.studentCode().toLowerCase().replace("-", "");

        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Tên đăng nhập đã tồn tại: " + username);
        }

        String password = req.password() != null && !req.password().isBlank()
                ? req.password()
                : "Student@123";

        Classroom classroom = classroomRepository.findById(req.classroomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        User user = userRepository.save(User.create(username, passwordEncoder.encode(password), Role.STUDENT));

        Student student = studentRepository.save(Student.create(
                user,
                req.studentCode(),
                req.fullName(),
                parseDate(req.dateOfBirth()),
                req.gender(),
                req.phone(),
                req.email(),
                classroom
        ));

        return StudentSummaryResponse.from(student);
    }

    @Transactional
    public StudentSummaryResponse updateStudent(Long id, UpdateStudentRequest req) {
        Student student = studentRepository.findByIdWithClassroom(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Classroom classroom = classroomRepository.findById(req.classroomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        student.update(req.fullName(), parseDate(req.dateOfBirth()), req.gender(), req.phone(), req.email(), classroom);

        return StudentSummaryResponse.from(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentSummaryResponse getStudent(Long id) {
        return StudentSummaryResponse.from(
                studentRepository.findByIdWithClassroom(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        );
    }

    private record ParsedRow(String studentCode, String fullName, String dateOfBirth,
                             String gender, String phone, String email,
                             String className, int rowNum) {}

    @Transactional
    public ImportResultResponse importFromExcel(MultipartFile file) {
        List<ParsedRow> parsedRows = new ArrayList<>();
        List<ImportErrorRow> errors = new ArrayList<>();

        // Pre-load classrooms to avoid N+1 in the loop
        java.util.Map<String, Classroom> classroomByName = classroomRepository.findAllWithDetails()
                .stream().collect(java.util.stream.Collectors.toMap(
                        cl -> cl.getName().toLowerCase(), cl -> cl));

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                String studentCode = cellStr(row, 0);
                String fullName    = cellStr(row, 1);
                String dateOfBirth = cellStr(row, 2);
                String gender      = cellStr(row, 3);
                String phone       = cellStr(row, 4);
                String email       = cellStr(row, 5);
                String className   = cellStr(row, 6);
                int rowNum = i + 1;

                if (studentCode.isBlank()) {
                    errors.add(new ImportErrorRow(rowNum, "Mã học sinh", "Không được để trống"));
                    continue;
                }
                if (fullName.isBlank()) {
                    errors.add(new ImportErrorRow(rowNum, "Họ và tên", "Không được để trống"));
                    continue;
                }
                if (className.isBlank() || !classroomByName.containsKey(className.toLowerCase())) {
                    errors.add(new ImportErrorRow(rowNum, "Lớp",
                            className.isBlank() ? "Không được để trống" : "Không tìm thấy lớp: " + className));
                    continue;
                }
                if (studentRepository.existsByStudentCode(studentCode)) {
                    errors.add(new ImportErrorRow(rowNum, "Mã học sinh", "Đã tồn tại: " + studentCode));
                    continue;
                }
                String username = studentCode.toLowerCase().replace("-", "");
                if (userRepository.existsByUsername(username)) {
                    errors.add(new ImportErrorRow(rowNum, "Tên đăng nhập", "Đã tồn tại: " + username));
                    continue;
                }
                if (!dateOfBirth.isBlank()) {
                    try {
                        LocalDate.parse(dateOfBirth, DATE_FMT);
                    } catch (DateTimeParseException e) {
                        errors.add(new ImportErrorRow(rowNum, "Ngày sinh", "Sai định dạng, dùng dd/MM/yyyy"));
                        continue;
                    }
                }

                parsedRows.add(new ParsedRow(studentCode, fullName, dateOfBirth,
                        gender, phone, email, className, rowNum));
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

        if (!errors.isEmpty()) {
            return ImportResultResponse.failed(errors);
        }

        // All rows valid — save all (all-or-nothing within this @Transactional)
        for (ParsedRow r : parsedRows) {
            Classroom classroom = classroomByName.get(r.className().toLowerCase());
            String username = r.studentCode().toLowerCase().replace("-", "");
            User user = userRepository.save(
                    User.create(username, passwordEncoder.encode("Student@123"), Role.STUDENT));
            studentRepository.save(Student.create(
                    user, r.studentCode(), r.fullName(),
                    r.dateOfBirth().isBlank() ? null : LocalDate.parse(r.dateOfBirth(), DATE_FMT),
                    r.gender(), r.phone(), r.email(), classroom));
        }

        return ImportResultResponse.ok(parsedRows.size());
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw, DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Sai định dạng ngày sinh, dùng dd/MM/yyyy");
        }
    }

    private String cellStr(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FMT);
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c <= 6; c++) {
            if (!cellStr(row, c).isBlank()) return false;
        }
        return true;
    }
}
