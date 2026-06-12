package vn.edu.fpt.myfptschool.grade.dto;

import java.util.List;

public record ClassroomSubjectGradeSheetResponse(
        Long classroomSubjectId,
        String subjectName,
        String classroomName,
        String teacherName,
        List<ScoreComponentDto> components,
        List<StudentGradeRow> students
) {}
