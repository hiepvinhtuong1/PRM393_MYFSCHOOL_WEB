package vn.edu.fpt.myfptschool.grade.dto;

import java.util.Map;

public record StudentGradeRow(
        Long studentId,
        String studentCode,
        String fullName,
        Map<String, GradeEntry> scores,
        Double average
) {}
