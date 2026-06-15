package vn.edu.fpt.myfptschool.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSubjectRequest(
        @NotBlank String name,
        String colorHex,
        @NotNull @Min(1) Integer coefficient
) {}
