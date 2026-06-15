package vn.edu.fpt.myfptschool.timetable.dto;

import vn.edu.fpt.myfptschool.timetable.entity.Lesson;
import vn.edu.fpt.myfptschool.timetable.entity.TimeSlot;

import java.time.format.DateTimeFormatter;

public record AdminLessonResponse(
        Long id,
        Long classroomSubjectId,
        String lessonDate,
        Short startSlotNumber,
        Short endSlotNumber,
        String startTime,
        String endTime,
        String slotLabel,
        Long roomId,
        String roomCode,
        String status,
        boolean hasMaterials,
        String note
) {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static AdminLessonResponse from(Lesson l) {
        TimeSlot start = l.getStartSlot();
        TimeSlot end = l.getEndSlot();
        String slotLabel = start.getSlotNumber().equals(end.getSlotNumber())
                ? "Tiết " + start.getSlotNumber()
                : "Tiết " + start.getSlotNumber() + "-" + end.getSlotNumber();

        return new AdminLessonResponse(
                l.getId(),
                l.getClassroomSubject().getId(),
                l.getLessonDate().toString(),
                start.getSlotNumber(),
                end.getSlotNumber(),
                start.getStartTime().format(TIME_FMT),
                end.getEndTime().format(TIME_FMT),
                slotLabel,
                l.getRoom() != null ? l.getRoom().getId() : null,
                l.getRoom() != null ? l.getRoom().getCode() : null,
                l.getStatus().name(),
                l.isHasMaterials(),
                l.getNote()
        );
    }
}
