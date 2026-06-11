package vn.edu.fpt.myfptschool.me.dto;

import lombok.Builder;
import lombok.Getter;
import vn.edu.fpt.myfptschool.student.entity.Student;

import java.time.LocalDate;

@Getter
@Builder
public class StudentProfileResponse {

    private String studentCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String classroomName;
    private String campusName;

    public static StudentProfileResponse from(Student student) {
        String classroomName = student.getClassroom() != null ? student.getClassroom().getName() : null;
        String campusName = student.getClassroom() != null && student.getClassroom().getCampus() != null
                ? student.getClassroom().getCampus().getName() : null;

        return StudentProfileResponse.builder()
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .phone(student.getPhone())
                .email(student.getEmail())
                .classroomName(classroomName)
                .campusName(campusName)
                .build();
    }
}
