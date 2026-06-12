package vn.edu.fpt.myfptschool.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.attendance.dto.LessonAttendanceResponse;
import vn.edu.fpt.myfptschool.attendance.dto.SubmitAttendanceRequest;
import vn.edu.fpt.myfptschool.attendance.service.AdminAttendanceService;

@RestController
@RequestMapping("/api/v1/admin/lessons")
@RequiredArgsConstructor
@Tag(name = "Admin - Attendance")
@SecurityRequirement(name = "bearerAuth")
public class AdminAttendanceController {

    private final AdminAttendanceService attendanceService;

    @GetMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Lấy danh sách điểm danh theo tiết học")
    public ResponseEntity<LessonAttendanceResponse> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendance(id));
    }

    @PostMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Điểm danh / cập nhật điểm danh cho tiết học")
    public ResponseEntity<Void> submitAttendance(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAttendanceRequest request
    ) {
        attendanceService.submitAttendance(id, request);
        return ResponseEntity.ok().build();
    }
}
