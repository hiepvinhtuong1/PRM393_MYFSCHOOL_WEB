package vn.edu.fpt.myfptschool.grade.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpsertGradeEntryRequest(
        @NotNull Long studentId,
        @NotNull Short componentId,
        @DecimalMin("0") @DecimalMax("10") BigDecimal score
) {}
