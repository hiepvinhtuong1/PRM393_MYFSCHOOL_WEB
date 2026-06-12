package vn.edu.fpt.myfptschool.grade.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateGradeRequest(
        @DecimalMin("0") @DecimalMax("10") BigDecimal score
) {}
