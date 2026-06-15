package vn.edu.fpt.myfptschool.student.dto;

import vn.edu.fpt.myfptschool.student.entity.Student;

public record StudentAdminResponse(
        Long id,
        String studentCode,
        String fullName,
        String dateOfBirth,
        String gender,
        String phone,
        String email,
        String photoUrl,
        Long classroomId,
        String classroomName,
        String username,
        boolean active
) {
    public static StudentAdminResponse from(Student s) {
        return new StudentAdminResponse(
                s.getId(),
                s.getStudentCode(),
                s.getFullName(),
                s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : null,
                s.getGender(),
                s.getPhone(),
                s.getEmail(),
                s.getPhotoUrl(),
                s.getClassroom() != null ? s.getClassroom().getId() : null,
                s.getClassroom() != null ? s.getClassroom().getName() : null,
                s.getUser() != null ? s.getUser().getUsername() : null,
                s.getUser() != null && s.getUser().isActive()
        );
    }
}
