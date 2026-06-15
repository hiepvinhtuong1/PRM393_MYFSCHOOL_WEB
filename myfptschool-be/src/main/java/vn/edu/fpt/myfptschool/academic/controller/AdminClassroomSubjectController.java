package vn.edu.fpt.myfptschool.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.academic.dto.*;
import vn.edu.fpt.myfptschool.academic.service.AdminClassroomSubjectService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Academic", description = "Classroom subjects, subjects, semesters")
@SecurityRequirement(name = "bearerAuth")
public class AdminClassroomSubjectController {

    private final AdminClassroomSubjectService service;

    @GetMapping("/classroom-subjects")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "List classroom-subject assignments (filter by classroomId and/or semesterId)")
    public ResponseEntity<ClassroomSubjectPageResponse> getClassroomSubjects(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.getClassroomSubjects(classroomId, semesterId, page, size));
    }

    @PostMapping("/classroom-subjects")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign a subject to a classroom for a semester")
    public ResponseEntity<ClassroomSubjectResponse> createClassroomSubject(
            @Valid @RequestBody CreateClassroomSubjectRequest request
    ) {
        return ResponseEntity.status(201).body(service.createClassroomSubject(request));
    }

    @DeleteMapping("/classroom-subjects/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa phân công giảng dạy (chỉ khi chưa có tiết học và điểm số)")
    public ResponseEntity<Void> deleteClassroomSubject(@PathVariable Long id) {
        service.deleteClassroomSubject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "List all subjects (for dropdowns)")
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {
        return ResponseEntity.ok(service.getAllSubjects());
    }

    @GetMapping("/semesters")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "List all semesters (for dropdowns)")
    public ResponseEntity<List<SemesterResponse>> getAllSemesters() {
        return ResponseEntity.ok(service.getAllSemesters());
    }
}
