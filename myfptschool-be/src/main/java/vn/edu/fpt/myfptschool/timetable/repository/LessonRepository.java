package vn.edu.fpt.myfptschool.timetable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.timetable.entity.Lesson;

import java.time.LocalDate;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("""
        SELECT l FROM Lesson l
        JOIN FETCH l.classroomSubject cs
        JOIN FETCH cs.subject s
        JOIN FETCH cs.teacher t
        LEFT JOIN FETCH l.room r
        JOIN FETCH l.startSlot ss
        JOIN FETCH l.endSlot es
        WHERE cs.classroom = :classroom
        AND l.lessonDate = :date
        ORDER BY ss.slotNumber
        """)
    List<Lesson> findByClassroomAndDate(
            @Param("classroom") Classroom classroom,
            @Param("date") LocalDate date
    );
}
