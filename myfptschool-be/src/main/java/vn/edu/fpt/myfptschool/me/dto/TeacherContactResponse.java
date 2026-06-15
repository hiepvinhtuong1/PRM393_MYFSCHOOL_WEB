package vn.edu.fpt.myfptschool.me.dto;

import vn.edu.fpt.myfptschool.teacher.entity.Teacher;

public record TeacherContactResponse(
        Long id,
        String name,
        String subject,
        String phone,
        String email,
        boolean isHomeroom
) {
    public static TeacherContactResponse ofHomeroom(Teacher teacher) {
        return new TeacherContactResponse(
                teacher.getId(),
                teacher.getFullName(),
                "GVCN",
                orEmpty(teacher.getPhone()),
                orEmpty(teacher.getEmail()),
                true
        );
    }

    public static TeacherContactResponse ofSubject(Teacher teacher, String subjectName) {
        return new TeacherContactResponse(
                teacher.getId(),
                teacher.getFullName(),
                subjectName,
                orEmpty(teacher.getPhone()),
                orEmpty(teacher.getEmail()),
                false
        );
    }

    private static String orEmpty(String value) {
        return value != null ? value : "";
    }
}
