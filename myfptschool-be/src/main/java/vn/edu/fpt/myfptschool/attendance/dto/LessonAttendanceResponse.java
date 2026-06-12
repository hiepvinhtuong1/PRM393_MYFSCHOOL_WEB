package vn.edu.fpt.myfptschool.attendance.dto;

import java.util.List;

public record LessonAttendanceResponse(
        Long lessonId,
        String lessonDate,
        String subjectName,
        String classroomName,
        String teacherName,
        String startTime,
        String endTime,
        String lessonStatus,
        int totalStudents,
        long totalPresent,
        long totalAbsent,
        long totalUnrecorded,
        List<StudentAttendanceEntry> entries
) {}
