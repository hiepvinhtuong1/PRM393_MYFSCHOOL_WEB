package vn.edu.fpt.myfptschool.student.dto;

import java.util.List;

public record ImportResultResponse(
        boolean success,
        int imported,
        List<ImportErrorRow> errors
) {
    public static ImportResultResponse ok(int count) {
        return new ImportResultResponse(true, count, List.of());
    }

    public static ImportResultResponse failed(List<ImportErrorRow> errors) {
        return new ImportResultResponse(false, 0, errors);
    }
}
