package vn.edu.fpt.myfptschool.attendance.dto;

import vn.edu.fpt.myfptschool.attendance.entity.AttendanceRecord;
import vn.edu.fpt.myfptschool.timetable.entity.Lesson;
import vn.edu.fpt.myfptschool.timetable.entity.TimeSlot;

public record AttendanceSessionResponse(
        String date,
        String slotLabel,
        String subjectName,
        String status
) {
    public static AttendanceSessionResponse from(AttendanceRecord record) {
        Lesson lesson = record.getLesson();
        TimeSlot start = lesson.getStartSlot();
        TimeSlot end = lesson.getEndSlot();

        String slotLabel = start.getSlotNumber().equals(end.getSlotNumber())
                ? "Tiết " + start.getSlotNumber()
                : "Tiết " + start.getSlotNumber() + "-" + end.getSlotNumber();

        return new AttendanceSessionResponse(
                lesson.getLessonDate().toString(),
                slotLabel,
                lesson.getClassroomSubject().getSubject().getName(),
                record.getStatus().name()
        );
    }
}
