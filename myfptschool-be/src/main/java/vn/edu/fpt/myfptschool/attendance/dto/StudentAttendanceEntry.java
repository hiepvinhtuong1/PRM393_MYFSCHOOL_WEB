package vn.edu.fpt.myfptschool.attendance.dto;

import vn.edu.fpt.myfptschool.attendance.entity.AttendanceStatus;

public record StudentAttendanceEntry(
        Long studentId,
        String studentCode,
        String fullName,
        AttendanceStatus status,
        String note,
        boolean recorded
) {}
