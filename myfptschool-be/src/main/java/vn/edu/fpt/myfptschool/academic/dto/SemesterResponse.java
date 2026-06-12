package vn.edu.fpt.myfptschool.academic.dto;

import vn.edu.fpt.myfptschool.academic.entity.Semester;

public record SemesterResponse(
        Long id,
        String name,
        String academicYear,
        String startDate,
        String endDate
) {
    public static SemesterResponse from(Semester s) {
        return new SemesterResponse(
                s.getId(),
                s.getName(),
                s.getAcademicYear().getLabel(),
                s.getStartDate().toString(),
                s.getEndDate().toString()
        );
    }
}
