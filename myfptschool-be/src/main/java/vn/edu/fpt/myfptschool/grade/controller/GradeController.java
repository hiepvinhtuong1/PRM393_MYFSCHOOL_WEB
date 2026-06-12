package vn.edu.fpt.myfptschool.grade.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.myfptschool.common.dto.ApiResponse;
import vn.edu.fpt.myfptschool.grade.dto.GradeSubjectResponse;
import vn.edu.fpt.myfptschool.grade.service.GradeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/grades")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<List<GradeSubjectResponse>>> getGrades(
            Authentication authentication,
            @RequestParam Long semesterId,
            @RequestParam(required = false) Long studentId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                gradeService.getGrades(authentication.getName(), semesterId, studentId)
        ));
    }
}
