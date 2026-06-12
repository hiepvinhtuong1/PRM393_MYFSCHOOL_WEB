package vn.edu.fpt.myfptschool.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.academic.dto.ClassroomResponse;
import vn.edu.fpt.myfptschool.academic.service.AdminClassroomService;
import vn.edu.fpt.myfptschool.student.dto.StudentSummaryResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Classrooms", description = "Classroom management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminClassroomController {

    private final AdminClassroomService adminClassroomService;

    @GetMapping("/classrooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all classrooms with student count")
    public ResponseEntity<List<ClassroomResponse>> getAllClassrooms() {
        return ResponseEntity.ok(adminClassroomService.getAllClassrooms());
    }

    @GetMapping("/classrooms/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get paginated students in a classroom")
    public ResponseEntity<Page<StudentSummaryResponse>> getStudentsByClassroom(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size
    ) {
        return ResponseEntity.ok(adminClassroomService.getStudentsByClassroom(id, page, size));
    }
}
