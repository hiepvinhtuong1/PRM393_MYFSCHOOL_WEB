package vn.edu.fpt.myfptschool.student.dto;

import java.util.List;

public record ImportResultResponse(
        boolean success,
        int imported,
        int parentsCreated,
        List<ImportErrorRow> errors
) {
    public static ImportResultResponse ok(int imported, int parentsCreated) {
        return new ImportResultResponse(true, imported, parentsCreated, List.of());
    }

    public static ImportResultResponse failed(List<ImportErrorRow> errors) {
        return new ImportResultResponse(false, 0, 0, errors);
    }
}
