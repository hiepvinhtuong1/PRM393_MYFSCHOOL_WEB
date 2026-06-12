package vn.edu.fpt.myfptschool.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.fpt.myfptschool.attendance.dto.AttendanceSubjectResponse;
import vn.edu.fpt.myfptschool.attendance.service.AttendanceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "Me - Attendance", description = "Attendance endpoints for students and parents")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    @Operation(summary = "Get attendance summary by semester")
    public ResponseEntity<List<AttendanceSubjectResponse>> getAttendance(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long semesterId,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(
                attendanceService.getAttendance(userDetails.getUsername(), semesterId, studentId)
        );
    }
}
