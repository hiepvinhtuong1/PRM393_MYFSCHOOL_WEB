package vn.edu.fpt.myfptschool.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStudentRequest(
        @NotBlank String fullName,
        String dateOfBirth,
        String gender,
        String phone,
        String email,
        @NotNull Long classroomId
) {}
