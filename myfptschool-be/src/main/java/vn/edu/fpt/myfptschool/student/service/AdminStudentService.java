package vn.edu.fpt.myfptschool.student.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import vn.edu.fpt.myfptschool.parent.entity.Parent;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.dto.*;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStudentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[3-9]\\d{8}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern CCCD_PATTERN  = Pattern.compile("^(\\d{9}|\\d{12})$");

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Single CRUD
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public StudentPageResponse getAllStudents(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> result = (search != null && !search.isBlank())
                ? studentRepository.findByFullNameContainingIgnoreCase(search, pageable)
                : studentRepository.findAll(pageable);
        return new StudentPageResponse(
                result.getContent().stream().map(StudentAdminResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public StudentAdminResponse createStudent(CreateStudentRequest req) {
        String username = (req.username() != null && !req.username().isBlank())
                ? req.username()
                : generateUsername(studentRepository.count() + 1);

        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Tên đăng nhập đã tồn tại: " + username);
        }

        Classroom classroom = classroomRepository.findById(req.classroomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        String password = (req.password() != null && !req.password().isBlank())
                ? req.password()
                : "Student@123";

        String studentCode = String.format("HS%d%05d", LocalDate.now().getYear(),
                studentRepository.count() + 1);

        User user = userRepository.save(User.create(username, passwordEncoder.encode(password), Role.STUDENT));

        Student student = studentRepository.save(Student.create(
                user, studentCode, req.fullName(),
                parseStudentDate(req.dateOfBirth()), req.gender(), req.phone(),
                req.email(), req.photoUrl(), classroom));

        return StudentAdminResponse.from(student);
    }

    @Transactional
    public StudentAdminResponse updateStudent(Long id, UpdateStudentRequest req) {
        Student student = studentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Classroom classroom = classroomRepository.findById(req.classroomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        student.update(req.fullName(), parseStudentDate(req.dateOfBirth()), req.gender(),
                req.phone(), req.email(), req.photoUrl(), classroom);

        return StudentAdminResponse.from(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentAdminResponse getStudent(Long id) {
        return StudentAdminResponse.from(
                studentRepository.findByIdWithDetails(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        );
    }

    // -------------------------------------------------------------------------
    // Excel import — cols: 0=Họ tên, 1=Ngày sinh, 2=Giới tính, 3=SĐT,
    //                      4=Email, 5=Lớp, 6=CCCD PH, 7=Họ tên PH,
    //                      8=Ngày sinh PH, 9=Giới tính PH, 10=SĐT PH, 11=Email PH
    // -------------------------------------------------------------------------

    private record ParsedRow(
            String fullName, String dateOfBirth, String gender, String phone, String email, String className,
            String parentCode, String parentFullName, String parentDateOfBirth,
            String parentGender, String parentPhone, String parentEmail,
            int rowNum
    ) {
        boolean hasParent() { return !parentCode.isBlank(); }
    }

    @Transactional
    public ImportResultResponse importFromExcel(MultipartFile file) {
        List<ParsedRow> parsedRows = new ArrayList<>();
        List<ImportErrorRow> errors = new ArrayList<>();

        Map<String, Classroom> classroomByName = classroomRepository.findAllWithDetails()
                .stream().collect(Collectors.toMap(cl -> cl.getName().toLowerCase(), cl -> cl));

        Set<String> validatedParentCodes = new HashSet<>();
        Set<String> seenFullNames = new HashSet<>(); // detect duplicate rows in batch

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                int rowNum = i + 1;

                String fullName    = cellStr(row, 0);
                String dateOfBirth = cellStr(row, 1);
                String gender      = cellStr(row, 2);
                String phone       = cellStr(row, 3);
                String email       = cellStr(row, 4);
                String className   = cellStr(row, 5);
                String parentCode  = cellStr(row, 6);
                String parentFullName  = cellStr(row, 7);
                String parentDob       = cellStr(row, 8);
                String parentGender    = cellStr(row, 9);
                String parentPhone     = cellStr(row, 10);
                String parentEmail     = cellStr(row, 11);

                // --- Validate student ---
                if (fullName.isBlank()) {
                    errors.add(new ImportErrorRow(rowNum, "Họ và tên", "Không được để trống"));
                    continue;
                }
                if (fullName.length() < 2 || fullName.length() > 100) {
                    errors.add(new ImportErrorRow(rowNum, "Họ và tên", "Phải từ 2 đến 100 ký tự"));
                    continue;
                }
                if (className.isBlank() || !classroomByName.containsKey(className.toLowerCase())) {
                    errors.add(new ImportErrorRow(rowNum, "Lớp",
                            className.isBlank() ? "Không được để trống" : "Không tìm thấy lớp: " + className));
                    continue;
                }
                if (!phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
                    errors.add(new ImportErrorRow(rowNum, "Số điện thoại", "Không hợp lệ (10 số, bắt đầu 03-09)"));
                    continue;
                }
                if (!email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
                    errors.add(new ImportErrorRow(rowNum, "Email", "Không hợp lệ"));
                    continue;
                }
                if (!dateOfBirth.isBlank()) {
                    String err = validateDateAge(dateOfBirth, 14, 20);
                    if (err != null) { errors.add(new ImportErrorRow(rowNum, "Ngày sinh HS", err)); continue; }
                }

                // --- Validate parent (only when CCCD provided and not yet validated in batch) ---
                if (!parentCode.isBlank()) {
                    boolean inBatch = validatedParentCodes.contains(parentCode);
                    boolean inDB    = !inBatch && parentRepository.existsByParentCode(parentCode);

                    if (!inBatch && !inDB) {
                        if (!CCCD_PATTERN.matcher(parentCode).matches()) {
                            errors.add(new ImportErrorRow(rowNum, "CCCD/CMND phụ huynh", "Phải là 9 hoặc 12 chữ số"));
                            continue;
                        }
                        if (parentFullName.isBlank()) {
                            errors.add(new ImportErrorRow(rowNum, "Họ tên phụ huynh", "Không được để trống khi có CCCD"));
                            continue;
                        }
                        if (parentFullName.length() < 2 || parentFullName.length() > 100) {
                            errors.add(new ImportErrorRow(rowNum, "Họ tên phụ huynh", "Phải từ 2 đến 100 ký tự"));
                            continue;
                        }
                        if (!parentPhone.isBlank() && !PHONE_PATTERN.matcher(parentPhone).matches()) {
                            errors.add(new ImportErrorRow(rowNum, "SĐT phụ huynh", "Không hợp lệ (10 số, bắt đầu 03-09)"));
                            continue;
                        }
                        if (!parentEmail.isBlank() && !EMAIL_PATTERN.matcher(parentEmail).matches()) {
                            errors.add(new ImportErrorRow(rowNum, "Email phụ huynh", "Không hợp lệ"));
                            continue;
                        }
                        if (!parentDob.isBlank()) {
                            String err = validateDateAge(parentDob, 25, 999);
                            if (err != null) { errors.add(new ImportErrorRow(rowNum, "Ngày sinh phụ huynh", err)); continue; }
                        }
                        if (userRepository.existsByUsername(parentCode)) {
                            errors.add(new ImportErrorRow(rowNum, "CCCD phụ huynh", "Tài khoản đã tồn tại với CCCD: " + parentCode));
                            continue;
                        }
                        validatedParentCodes.add(parentCode);
                    }
                }

                parsedRows.add(new ParsedRow(
                        fullName, dateOfBirth, gender, phone, email, className,
                        parentCode, parentFullName, parentDob, parentGender, parentPhone, parentEmail,
                        rowNum));
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

        if (!errors.isEmpty()) {
            return ImportResultResponse.failed(errors);
        }

        // --- Save all (all-or-nothing) ---
        long baseStudentCount = studentRepository.count();
        Map<String, Parent> inBatchParents = new HashMap<>();
        int parentsCreated = 0;
        int year = LocalDate.now().getYear();

        for (int idx = 0; idx < parsedRows.size(); idx++) {
            ParsedRow r = parsedRows.get(idx);

            String studentCode = String.format("HS%d%05d", year, baseStudentCount + idx + 1);
            String studentUsername = generateUsername(baseStudentCount + idx + 1);

            Classroom classroom = classroomByName.get(r.className().toLowerCase());
            User studentUser = userRepository.save(
                    User.create(studentUsername, passwordEncoder.encode("Student@123"), Role.STUDENT));
            Student student = studentRepository.save(Student.create(
                    studentUser, studentCode, r.fullName(),
                    r.dateOfBirth().isBlank() ? null : LocalDate.parse(r.dateOfBirth(), DATE_FMT),
                    r.gender(), r.phone(), r.email(), null, classroom));

            if (r.hasParent()) {
                Parent parent = inBatchParents.get(r.parentCode());
                if (parent == null) {
                    parent = parentRepository.findByParentCode(r.parentCode()).orElse(null);
                }
                if (parent == null) {
                    User parentUser = userRepository.save(
                            User.create(r.parentCode(), passwordEncoder.encode("Parent@123"), Role.PARENT));
                    parent = parentRepository.save(Parent.create(
                            parentUser, r.parentCode(), r.parentFullName(),
                            r.parentDateOfBirth().isBlank() ? null : LocalDate.parse(r.parentDateOfBirth(), DATE_FMT),
                            r.parentGender(), r.parentPhone(), r.parentEmail()));
                    parentsCreated++;
                }
                inBatchParents.put(r.parentCode(), parent);
                parent.addChild(student);
                parentRepository.save(parent);
            }
        }

        return ImportResultResponse.ok(parsedRows.size(), parentsCreated);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Username pattern: hs + zero-padded sequence, e.g. hs00001 */
    private String generateUsername(long seq) {
        return String.format("hs%05d", seq);
    }

    private LocalDate parseStudentDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            LocalDate dob = LocalDate.parse(raw, DATE_FMT);
            int age = calcAge(dob);
            if (age < 14 || age > 20) {
                throw new AppException(ErrorCode.VALIDATION_FAILED,
                        "Học sinh phải trong độ tuổi từ 14 đến 20 (hiện tại " + age + " tuổi)");
            }
            return dob;
        } catch (DateTimeParseException e) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Sai định dạng ngày sinh, dùng dd/MM/yyyy");
        }
    }

    private String validateDateAge(String raw, int minAge, int maxAge) {
        try {
            LocalDate dob = LocalDate.parse(raw, DATE_FMT);
            int age = calcAge(dob);
            if (age < minAge || age > maxAge) {
                String range = maxAge >= 999 ? "ít nhất " + minAge : minAge + "-" + maxAge;
                return "Tuổi phải " + range + " (hiện tại " + age + " tuổi)";
            }
            return null;
        } catch (DateTimeParseException e) {
            return "Sai định dạng, dùng dd/MM/yyyy";
        }
    }

    private int calcAge(LocalDate dob) {
        LocalDate today = LocalDate.now();
        int age = today.getYear() - dob.getYear();
        if (dob.plusYears(age).isAfter(today)) age--;
        return age;
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
        for (int c = 0; c <= 5; c++) {
            if (!cellStr(row, c).isBlank()) return false;
        }
        return true;
    }
}
