package vn.edu.fpt.myfptschool.student.dto;

import vn.edu.fpt.myfptschool.student.entity.Student;

public record StudentSummaryResponse(
        Long id,
        String studentCode,
        String fullName,
        String dateOfBirth,
        String gender,
        String phone,
        String email,
        String photoUrl
) {
    public static StudentSummaryResponse from(Student s) {
        return new StudentSummaryResponse(
                s.getId(),
                s.getStudentCode(),
                s.getFullName(),
                s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : null,
                s.getGender(),
                s.getPhone(),
                s.getEmail(),
                s.getPhotoUrl()
        );
    }
}
