package vn.edu.fpt.myfptschool.grade.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.grade.dto.BulkUpsertGradesRequest;
import vn.edu.fpt.myfptschool.grade.dto.ClassroomSubjectGradeSheetResponse;
import vn.edu.fpt.myfptschool.grade.dto.UpdateGradeRequest;
import vn.edu.fpt.myfptschool.grade.service.AdminGradeService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin - Grades")
@SecurityRequirement(name = "bearerAuth")
public class AdminGradeController {

    private final AdminGradeService adminGradeService;

    @GetMapping("/api/v1/admin/classroom-subjects/{id}/grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Lấy bảng điểm theo môn học lớp")
    public ResponseEntity<ClassroomSubjectGradeSheetResponse> getGradeSheet(@PathVariable Long id) {
        return ResponseEntity.ok(adminGradeService.getGradeSheet(id));
    }

    @PostMapping("/api/v1/admin/classroom-subjects/{id}/grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Nhập / cập nhật điểm hàng loạt theo môn học lớp")
    public ResponseEntity<Void> upsertGrades(
            @PathVariable Long id,
            @Valid @RequestBody BulkUpsertGradesRequest request
    ) {
        adminGradeService.upsertGrades(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/admin/classroom-subjects/{id}/grades/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Xuất bảng điểm ra Excel")
    public ResponseEntity<byte[]> exportGradeSheet(@PathVariable Long id) {
        byte[] data = adminGradeService.exportGradeSheet(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bangdiem_" + id + ".xlsx\"")
                .body(data);
    }

    @PutMapping("/api/v1/admin/grades/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Sửa điểm một bản ghi")
    public ResponseEntity<Void> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGradeRequest request
    ) {
        adminGradeService.updateGrade(id, request);
        return ResponseEntity.ok().build();
    }
}
