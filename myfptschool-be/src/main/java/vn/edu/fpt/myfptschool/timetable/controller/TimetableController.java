package vn.edu.fpt.myfptschool.timetable.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.common.dto.ApiResponse;
import vn.edu.fpt.myfptschool.timetable.dto.LessonResponse;
import vn.edu.fpt.myfptschool.timetable.service.TimetableService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/timetable")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getTimetable(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                timetableService.getTimetable(authentication.getName(), date, studentId)
        ));
    }
}
