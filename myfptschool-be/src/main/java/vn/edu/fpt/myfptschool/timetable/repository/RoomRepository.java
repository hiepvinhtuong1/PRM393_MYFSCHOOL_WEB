package vn.edu.fpt.myfptschool.timetable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.timetable.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
