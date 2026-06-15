package vn.edu.fpt.myfptschool.timetable.dto;

import vn.edu.fpt.myfptschool.timetable.entity.TimeSlot;

public record TimeSlotResponse(Short id, Short slotNumber, String startTime, String endTime) {
    public static TimeSlotResponse from(TimeSlot ts) {
        return new TimeSlotResponse(ts.getId(), ts.getSlotNumber(),
                ts.getStartTime().toString(), ts.getEndTime().toString());
    }
}
