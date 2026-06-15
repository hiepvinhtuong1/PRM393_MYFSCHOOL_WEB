package vn.edu.fpt.myfptschool.teacher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.auth.dto.ResetPasswordRequest;
import vn.edu.fpt.myfptschool.auth.dto.ToggleStatusRequest;
import vn.edu.fpt.myfptschool.teacher.dto.*;
import vn.edu.fpt.myfptschool.teacher.service.AdminTeacherService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Teachers", description = "Teacher management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminTeacherController {

    private final AdminTeacherService adminTeacherService;

    @GetMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get paginated teacher list with optional name search")
    public ResponseEntity<TeacherPageResponse> getAllTeachers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminTeacherService.getAllTeachers(search, page, size));
    }

    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new teacher (also creates login account)")
    public ResponseEntity<TeacherResponse> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request
    ) {
        return ResponseEntity.status(201).body(adminTeacherService.createTeacher(request));
    }

    @PutMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update teacher info")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeacherRequest request
    ) {
        return ResponseEntity.ok(adminTeacherService.updateTeacher(id, request));
    }

    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get teacher detail")
    public ResponseEntity<TeacherResponse> getTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(adminTeacherService.getTeacher(id));
    }

    @PatchMapping("/teachers/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate a teacher account")
    public ResponseEntity<Void> toggleTeacherStatus(
            @PathVariable Long id,
            @Valid @RequestBody ToggleStatusRequest request
    ) {
        adminTeacherService.toggleStatus(id, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/teachers/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset teacher account password")
    public ResponseEntity<Void> resetTeacherPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        adminTeacherService.resetPassword(id, request);
        return ResponseEntity.ok().build();
    }
}
