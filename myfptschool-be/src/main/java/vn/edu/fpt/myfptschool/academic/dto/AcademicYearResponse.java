package vn.edu.fpt.myfptschool.academic.dto;

import vn.edu.fpt.myfptschool.academic.entity.AcademicYear;

public record AcademicYearResponse(Long id, String label, String startDate, String endDate) {
    public static AcademicYearResponse from(AcademicYear ay) {
        return new AcademicYearResponse(
                ay.getId(), ay.getLabel(),
                ay.getStartDate().toString(), ay.getEndDate().toString()
        );
    }
}
