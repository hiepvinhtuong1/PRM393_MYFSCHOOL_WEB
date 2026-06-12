package vn.edu.fpt.myfptschool.grade.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkUpsertGradesRequest(
        @NotNull @NotEmpty List<@Valid UpsertGradeEntryRequest> entries
) {}
