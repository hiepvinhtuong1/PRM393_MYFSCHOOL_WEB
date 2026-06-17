package vn.edu.fpt.myfptschool.me.dto;

import vn.edu.fpt.myfptschool.academic.entity.Semester;

import java.time.LocalDate;

public record SemesterResponse(Long id, String name, String academicYear, LocalDate startDate, LocalDate endDate) {

    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
                semester.getId(),
                semester.getName(),
                semester.getAcademicYear().getLabel(),
                semester.getStartDate(),
                semester.getEndDate()
        );
    }
}
