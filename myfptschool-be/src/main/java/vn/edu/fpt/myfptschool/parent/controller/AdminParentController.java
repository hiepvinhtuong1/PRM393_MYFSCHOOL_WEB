package vn.edu.fpt.myfptschool.parent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.parent.dto.*;
import vn.edu.fpt.myfptschool.parent.service.AdminParentService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Parents", description = "Parent management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminParentController {

    private final AdminParentService adminParentService;

    @GetMapping("/parents")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get paginated parent list with optional name search")
    public ResponseEntity<vn.edu.fpt.myfptschool.parent.dto.ParentPageResponse> getAllParents(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminParentService.getAllParents(search, page, size));
    }

    @PostMapping("/parents")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new parent (also creates login account)")
    public ResponseEntity<ParentResponse> createParent(
            @Valid @RequestBody CreateParentRequest request
    ) {
        return ResponseEntity.status(201).body(adminParentService.createParent(request));
    }

    @PutMapping("/parents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update parent info")
    public ResponseEntity<ParentResponse> updateParent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateParentRequest request
    ) {
        return ResponseEntity.ok(adminParentService.updateParent(id, request));
    }

    @GetMapping("/parents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get parent detail with linked children")
    public ResponseEntity<ParentResponse> getParent(@PathVariable Long id) {
        return ResponseEntity.ok(adminParentService.getParent(id));
    }

    @PostMapping("/students/{studentId}/parents/{parentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Link a parent to a student (max 2 parents per student)")
    public ResponseEntity<ParentResponse> linkParentToStudent(
            @PathVariable Long studentId,
            @PathVariable Long parentId
    ) {
        return ResponseEntity.ok(adminParentService.linkParentToStudent(studentId, parentId));
    }

    @PatchMapping("/parents/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bật / tắt tài khoản phụ huynh")
    public ResponseEntity<ParentResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminParentService.toggleStatus(id));
    }

    @DeleteMapping("/parents/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gỡ liên kết phụ huynh – học sinh")
    public ResponseEntity<ParentResponse> unlinkStudent(
            @PathVariable Long id,
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(adminParentService.unlinkStudent(id, studentId));
    }
}
