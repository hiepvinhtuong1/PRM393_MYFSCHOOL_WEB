package vn.edu.fpt.myfptschool.teacher.dto;

import vn.edu.fpt.myfptschool.teacher.entity.Teacher;

public record TeacherResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        Long campusId,
        String campusName,
        String username
) {
    public static TeacherResponse from(Teacher t) {
        return new TeacherResponse(
                t.getId(),
                t.getFullName(),
                t.getPhone(),
                t.getEmail(),
                t.getCampus() != null ? t.getCampus().getId() : null,
                t.getCampus() != null ? t.getCampus().getName() : null,
                t.getUser() != null ? t.getUser().getUsername() : null
        );
    }
}
