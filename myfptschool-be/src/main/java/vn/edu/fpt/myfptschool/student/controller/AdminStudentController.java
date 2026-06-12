package vn.edu.fpt.myfptschool.student.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.myfptschool.student.dto.*;
import vn.edu.fpt.myfptschool.student.service.AdminStudentService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Students", description = "Student management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminStudentController {

    private final AdminStudentService adminStudentService;

    @PostMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new student (also creates login account)")
    public ResponseEntity<StudentSummaryResponse> createStudent(
            @Valid @RequestBody CreateStudentRequest request
    ) {
        return ResponseEntity.status(201).body(adminStudentService.createStudent(request));
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update student info")
    public ResponseEntity<StudentSummaryResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request
    ) {
        return ResponseEntity.ok(adminStudentService.updateStudent(id, request));
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get student detail")
    public ResponseEntity<StudentSummaryResponse> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(adminStudentService.getStudent(id));
    }

    @GetMapping("/students/import/template")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Download Excel template for bulk student import")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] bytes = buildTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_import_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping(value = "/students/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk import students from Excel (all-or-nothing)")
    public ResponseEntity<ImportResultResponse> importStudents(
            @RequestPart("file") MultipartFile file
    ) {
        ImportResultResponse result = adminStudentService.importFromExcel(file);
        return result.success()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    private byte[] buildTemplate() throws IOException {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Danh sách học sinh");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Student cols (blue, required) + parent cols (green, optional — CCCD as identifier)
            String[] headers = {
                "Họ và tên HS (*)", "Ngày sinh HS (dd/MM/yyyy)", "Giới tính HS",
                "SĐT HS", "Email HS", "Lớp (*)",
                "CCCD/CMND phụ huynh", "Họ tên phụ huynh", "Ngày sinh PH (dd/MM/yyyy)",
                "Giới tính PH", "SĐT phụ huynh", "Email phụ huynh"
            };

            CellStyle parentHeaderStyle = wb.createCellStyle();
            Font parentFont = wb.createFont();
            parentFont.setBold(true);
            parentHeaderStyle.setFont(parentFont);
            parentHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            parentHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(i < 6 ? headerStyle : parentHeaderStyle);
                sheet.setColumnWidth(i, 6500);
            }

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Trần Thị B");
            sample.createCell(1).setCellValue("20/05/2009");
            sample.createCell(2).setCellValue("Nữ");
            sample.createCell(3).setCellValue("0901234568");
            sample.createCell(4).setCellValue("b.tran@fpt.edu.vn");
            sample.createCell(5).setCellValue("12A1");
            sample.createCell(6).setCellValue("123456789012");
            sample.createCell(7).setCellValue("Nguyễn Văn C");
            sample.createCell(8).setCellValue("15/03/1980");
            sample.createCell(9).setCellValue("Nam");
            sample.createCell(10).setCellValue("0912345678");
            sample.createCell(11).setCellValue("c.nguyen@gmail.com");

            wb.write(out);
            return out.toByteArray();
        }
    }
}
