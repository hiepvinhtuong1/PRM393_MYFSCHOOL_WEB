package vn.edu.fpt.myfptschool.timetable.dto;

import vn.edu.fpt.myfptschool.timetable.entity.Room;

public record RoomResponse(Long id, String code, String campusName) {
    public static RoomResponse from(Room r) {
        return new RoomResponse(r.getId(), r.getCode(), r.getCampus().getName());
    }
}
