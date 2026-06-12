package vn.edu.fpt.myfptschool.academic.dto;

import java.util.List;

public record ClassroomSubjectPageResponse(
        List<ClassroomSubjectResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
