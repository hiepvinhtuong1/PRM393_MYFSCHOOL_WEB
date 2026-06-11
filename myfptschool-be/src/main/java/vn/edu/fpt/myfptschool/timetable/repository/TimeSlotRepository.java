package vn.edu.fpt.myfptschool.timetable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.timetable.entity.TimeSlot;

import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Short> {
    Optional<TimeSlot> findBySlotNumber(Short slotNumber);
}
