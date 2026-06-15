package vn.edu.fpt.myfptschool.timetable.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.timetable.dto.AdminLessonResponse;
import vn.edu.fpt.myfptschool.timetable.dto.CreateLessonRequest;
import vn.edu.fpt.myfptschool.timetable.dto.RoomResponse;
import vn.edu.fpt.myfptschool.timetable.dto.TimeSlotResponse;
import vn.edu.fpt.myfptschool.timetable.dto.UpdateLessonRequest;
import vn.edu.fpt.myfptschool.timetable.service.AdminLessonService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin - Lessons")
@SecurityRequirement(name = "bearerAuth")
public class AdminLessonController {

    private final AdminLessonService adminLessonService;

    @GetMapping("/api/v1/admin/classroom-subjects/{id}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách tiết học theo môn/lớp")
    public ResponseEntity<List<AdminLessonResponse>> getLessons(@PathVariable Long id) {
        return ResponseEntity.ok(adminLessonService.getLessons(id));
    }

    @PostMapping("/api/v1/admin/classroom-subjects/{id}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Tạo tiết học mới cho môn/lớp")
    public ResponseEntity<AdminLessonResponse> createLesson(
            @PathVariable Long id,
            @Valid @RequestBody CreateLessonRequest request
    ) {
        return ResponseEntity.ok(adminLessonService.createLesson(id, request));
    }

    @PatchMapping("/api/v1/admin/lessons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Cập nhật trạng thái / phòng / ghi chú tiết học")
    public ResponseEntity<AdminLessonResponse> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLessonRequest request
    ) {
        return ResponseEntity.ok(adminLessonService.updateLesson(id, request));
    }

    @DeleteMapping("/api/v1/admin/lessons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa tiết học (cascade xóa điểm danh)")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        adminLessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/admin/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách phòng học")
    public ResponseEntity<List<RoomResponse>> getRooms() {
        return ResponseEntity.ok(adminLessonService.getRooms());
    }

    @GetMapping("/api/v1/admin/time-slots")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách tiết (slot) trong ngày")
    public ResponseEntity<List<TimeSlotResponse>> getTimeSlots() {
        return ResponseEntity.ok(adminLessonService.getTimeSlots());
    }
}
