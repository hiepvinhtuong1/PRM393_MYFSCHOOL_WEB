package vn.edu.fpt.myfptschool.student.dto;

import java.util.List;

public record StudentPageResponse(
        List<StudentAdminResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
