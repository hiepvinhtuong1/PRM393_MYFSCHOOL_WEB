package vn.edu.fpt.myfptschool.parent.dto;

import java.util.List;

public record ParentPageResponse(
        List<ParentResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
