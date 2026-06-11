package vn.edu.fpt.myfptschool.me.dto;

import lombok.Builder;
import lombok.Getter;
import vn.edu.fpt.myfptschool.student.entity.Student;

@Getter
@Builder
public class ChildSummary {

    private String studentCode;
    private String fullName;
    private String classroomName;

    public static ChildSummary from(Student student) {
        return ChildSummary.builder()
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .classroomName(student.getClassroom() != null ? student.getClassroom().getName() : null)
                .build();
    }
}
