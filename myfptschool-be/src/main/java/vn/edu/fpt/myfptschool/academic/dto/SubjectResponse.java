package vn.edu.fpt.myfptschool.academic.dto;

import vn.edu.fpt.myfptschool.academic.entity.Subject;

public record SubjectResponse(
        Long id,
        String name,
        String colorHex,
        Short coefficient
) {
    public static SubjectResponse from(Subject s) {
        return new SubjectResponse(s.getId(), s.getName(), s.getColorHex(), s.getCoefficient());
    }
}
