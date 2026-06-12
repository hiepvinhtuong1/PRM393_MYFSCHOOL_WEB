package vn.edu.fpt.myfptschool.attendance.dto;

import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceRecord;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceStatus;

import java.util.List;

public record AttendanceSubjectResponse(
        Long classroomSubjectId,
        String subjectName,
        String teacherName,
        int totalSessions,
        int presentSessions,
        int lateSessions,
        int excusedAbsent,
        int unexcusedAbsent,
        int warningThreshold,
        String status,
        List<AttendanceSessionResponse> sessions
) {
    public static AttendanceSubjectResponse from(
            ClassroomSubject cs,
            int totalSessions,
            List<AttendanceRecord> records
    ) {
        int present = 0, late = 0, excused = 0, unexcused = 0;
        for (AttendanceRecord ar : records) {
            switch (ar.getStatus()) {
                case present -> present++;
                case late -> late++;
                case excused_absent -> excused++;
                case unexcused_absent -> unexcused++;
            }
        }

        int totalAbsent = excused + unexcused;
        int threshold = Math.max(1, (int) Math.round(totalSessions * 0.2));
        String status = computeStatus(totalAbsent, threshold);

        List<AttendanceSessionResponse> sessions = records.stream()
                .map(AttendanceSessionResponse::from)
                .toList();

        return new AttendanceSubjectResponse(
                cs.getId(),
                cs.getSubject().getName(),
                cs.getTeacher().getFullName(),
                totalSessions,
                present,
                late,
                excused,
                unexcused,
                threshold,
                status,
                sessions
        );
    }

    private static String computeStatus(int totalAbsent, int threshold) {
        if (totalAbsent > threshold) return "exceeded";
        if (totalAbsent >= (int) Math.ceil(threshold * 0.8)) return "danger";
        if (totalAbsent >= (int) Math.ceil(threshold * 0.5)) return "attention";
        return "safe";
    }
}
