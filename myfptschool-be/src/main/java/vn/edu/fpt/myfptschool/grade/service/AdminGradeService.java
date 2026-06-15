package vn.edu.fpt.myfptschool.grade.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;
import vn.edu.fpt.myfptschool.academic.repository.ClassroomSubjectRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.grade.dto.*;
import vn.edu.fpt.myfptschool.grade.entity.GradeRecord;
import vn.edu.fpt.myfptschool.grade.entity.ScoreComponent;
import vn.edu.fpt.myfptschool.grade.repository.GradeRecordRepository;
import vn.edu.fpt.myfptschool.grade.repository.ScoreComponentRepository;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminGradeService {

    private final ClassroomSubjectRepository classroomSubjectRepository;
    private final StudentRepository studentRepository;
    private final GradeRecordRepository gradeRecordRepository;
    private final ScoreComponentRepository scoreComponentRepository;

    @Transactional(readOnly = true)
    public ClassroomSubjectGradeSheetResponse getGradeSheet(Long classroomSubjectId) {
        ClassroomSubject cs = classroomSubjectRepository.findByIdWithDetails(classroomSubjectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Môn học lớp không tồn tại"));

        List<Student> students = studentRepository.findByClassroomOrderByFullName(cs.getClassroom());
        List<ScoreComponent> components = scoreComponentRepository.findAll()
                .stream().sorted(Comparator.comparing(ScoreComponent::getDisplayOrder)).toList();

        List<GradeRecord> records = gradeRecordRepository.findByClassroomSubjectWithDetails(cs);
        // Index: studentId → componentCode → GradeRecord
        Map<Long, Map<String, GradeRecord>> recordIndex = new HashMap<>();
        for (GradeRecord gr : records) {
            recordIndex
                    .computeIfAbsent(gr.getStudent().getId(), k -> new HashMap<>())
                    .put(gr.getComponent().getCode(), gr);
        }

        List<StudentGradeRow> rows = students.stream()
                .map(s -> buildStudentRow(s, components, recordIndex.getOrDefault(s.getId(), Map.of())))
                .toList();

        return new ClassroomSubjectGradeSheetResponse(
                cs.getId(),
                cs.getSubject().getName(),
                cs.getClassroom().getName(),
                cs.getTeacher().getFullName(),
                components.stream().map(ScoreComponentDto::from).toList(),
                rows
        );
    }

    @Transactional
    public void upsertGrades(Long classroomSubjectId, BulkUpsertGradesRequest request) {
        ClassroomSubject cs = classroomSubjectRepository.findByIdWithDetails(classroomSubjectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Môn học lớp không tồn tại"));

        List<Student> students = studentRepository.findByClassroomOrderByFullName(cs.getClassroom());
        Map<Long, Student> studentMap = students.stream().collect(Collectors.toMap(Student::getId, s -> s));

        Map<Short, ScoreComponent> componentMap = scoreComponentRepository.findAll()
                .stream().collect(Collectors.toMap(ScoreComponent::getId, c -> c));

        List<GradeRecord> toSave = new ArrayList<>();

        for (UpsertGradeEntryRequest entry : request.entries()) {
            Student student = studentMap.get(entry.studentId());
            if (student == null) {
                throw new AppException(ErrorCode.NOT_FOUND,
                        "Học sinh ID " + entry.studentId() + " không thuộc lớp này");
            }

            ScoreComponent component = componentMap.get(entry.componentId());
            if (component == null) {
                throw new AppException(ErrorCode.NOT_FOUND,
                        "Thành phần điểm ID " + entry.componentId() + " không tồn tại");
            }

            GradeRecord record = gradeRecordRepository
                    .findByClassroomSubjectAndStudentAndComponent(cs, student, component)
                    .orElse(null);

            if (record == null) {
                toSave.add(GradeRecord.create(student, cs, component, entry.score()));
            } else {
                record.update(entry.score());
                toSave.add(record);
            }
        }

        gradeRecordRepository.saveAll(toSave);
    }

    @Transactional
    public void updateGrade(Long gradeRecordId, UpdateGradeRequest request) {
        GradeRecord record = gradeRecordRepository.findById(gradeRecordId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Bản ghi điểm không tồn tại"));
        record.update(request.score());
        gradeRecordRepository.save(record);
    }

    private StudentGradeRow buildStudentRow(Student student, List<ScoreComponent> components,
                                            Map<String, GradeRecord> studentRecords) {
        Map<String, GradeEntry> scores = new LinkedHashMap<>();
        List<Double> txScores = new ArrayList<>();
        Double midterm = null;
        Double finalScore = null;

        for (ScoreComponent c : components) {
            GradeRecord gr = studentRecords.get(c.getCode());
            if (gr != null) {
                Double val = gr.getScore() != null ? gr.getScore().doubleValue() : null;
                scores.put(c.getCode(), new GradeEntry(gr.getId(), val));
                if (c.getCode().startsWith("TX") && val != null) txScores.add(val);
                else if ("DGKK".equals(c.getCode())) midterm = val;
                else if ("DCK".equals(c.getCode())) finalScore = val;
            } else {
                scores.put(c.getCode(), new GradeEntry(null, null));
            }
        }

        Double average = computeAverage(txScores, midterm, finalScore);
        return new StudentGradeRow(student.getId(), student.getStudentCode(), student.getFullName(), scores, average);
    }

    public byte[] exportGradeSheet(Long classroomSubjectId) {
        ClassroomSubjectGradeSheetResponse sheet = getGradeSheet(classroomSubjectId);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet ws = wb.createSheet("Bảng điểm");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row titleRow = ws.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(sheet.classroomName() + " — " + sheet.subjectName() + " — GV: " + sheet.teacherName());
            titleCell.setCellStyle(headerStyle);

            Row headerRow = ws.createRow(1);
            headerRow.createCell(0).setCellValue("Mã HS");
            headerRow.createCell(1).setCellValue("Họ tên");
            for (int i = 0; i < sheet.components().size(); i++) {
                ScoreComponentDto c = sheet.components().get(i);
                Cell cell = headerRow.createCell(2 + i);
                cell.setCellValue(c.code() + " (×" + c.weight() + ")");
                cell.setCellStyle(headerStyle);
            }
            Cell avgHeader = headerRow.createCell(2 + sheet.components().size());
            avgHeader.setCellValue("ĐTK");
            avgHeader.setCellStyle(headerStyle);

            for (int r = 0; r < sheet.students().size(); r++) {
                StudentGradeRow row = sheet.students().get(r);
                Row dataRow = ws.createRow(r + 2);
                dataRow.createCell(0).setCellValue(row.studentCode());
                dataRow.createCell(1).setCellValue(row.fullName());
                for (int c = 0; c < sheet.components().size(); c++) {
                    GradeEntry entry = row.scores().get(sheet.components().get(c).code());
                    Cell cell = dataRow.createCell(2 + c);
                    if (entry != null && entry.score() != null) {
                        cell.setCellValue(entry.score());
                    }
                }
                Cell avgCell = dataRow.createCell(2 + sheet.components().size());
                if (row.average() != null) {
                    avgCell.setCellValue(row.average());
                }
            }

            for (int i = 0; i <= 2 + sheet.components().size(); i++) {
                ws.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi xuất file Excel");
        }
    }

    // DTBm = (∑TX×1 + DGKK×2 + DCK×3) / (n_TX + 2 + 3)
    private Double computeAverage(List<Double> txScores, Double midterm, Double finalScore) {
        if (midterm == null || finalScore == null) return null;
        double sum = txScores.stream().mapToDouble(Double::doubleValue).sum()
                + midterm * 2 + finalScore * 3;
        int count = txScores.size() + 2 + 3;
        return BigDecimal.valueOf(sum / count).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
