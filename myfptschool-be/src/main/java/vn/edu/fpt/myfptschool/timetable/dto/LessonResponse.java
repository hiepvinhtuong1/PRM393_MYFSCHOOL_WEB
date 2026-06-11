package vn.edu.fpt.myfptschool.timetable.dto;

import vn.edu.fpt.myfptschool.timetable.entity.Lesson;
import vn.edu.fpt.myfptschool.timetable.entity.TimeSlot;

import java.time.format.DateTimeFormatter;

public record LessonResponse(
        Long id,
        String date,
        String subjectName,
        String teacherName,
        String startTime,
        String endTime,
        String slotLabel,
        String roomCode,
        String colorHex,
        String status,
        boolean hasMaterials
) {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static LessonResponse from(Lesson lesson) {
        TimeSlot start = lesson.getStartSlot();
        TimeSlot end = lesson.getEndSlot();

        String slotLabel = start.getSlotNumber().equals(end.getSlotNumber())
                ? "Tiết " + start.getSlotNumber()
                : "Tiết " + start.getSlotNumber() + "-" + end.getSlotNumber();

        return new LessonResponse(
                lesson.getId(),
                lesson.getLessonDate().toString(),
                lesson.getClassroomSubject().getSubject().getName(),
                lesson.getClassroomSubject().getTeacher().getFullName(),
                start.getStartTime().format(TIME_FMT),
                end.getEndTime().format(TIME_FMT),
                slotLabel,
                lesson.getRoom() != null ? lesson.getRoom().getCode() : null,
                lesson.getClassroomSubject().getSubject().getColorHex(),
                lesson.getStatus().name(),
                lesson.isHasMaterials()
        );
    }
}
