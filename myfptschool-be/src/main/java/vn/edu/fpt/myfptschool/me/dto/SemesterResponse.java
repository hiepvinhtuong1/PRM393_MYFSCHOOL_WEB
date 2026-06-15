package vn.edu.fpt.myfptschool.me.dto;

import vn.edu.fpt.myfptschool.academic.entity.Semester;

public record SemesterResponse(
        Long id,
        String name,
        String academicYear
) {
    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
                semester.getId(),
                semester.getName(),
                semester.getAcademicYear().getLabel()
        );
    }
}
