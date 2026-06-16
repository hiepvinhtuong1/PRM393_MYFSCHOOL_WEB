package vn.edu.fpt.myfptschool.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSemesterRequest(
        @NotNull Long academicYearId,
        @NotBlank String name,
        @NotBlank String startDate,
        @NotBlank String endDate
) {}
