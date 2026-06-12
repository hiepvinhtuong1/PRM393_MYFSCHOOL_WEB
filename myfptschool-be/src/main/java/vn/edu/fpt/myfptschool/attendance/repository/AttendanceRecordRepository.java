package vn.edu.fpt.myfptschool.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceRecord;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.timetable.entity.Lesson;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    @Query("""
        SELECT ar FROM AttendanceRecord ar
        JOIN FETCH ar.lesson l
        JOIN FETCH l.classroomSubject cs
        JOIN FETCH cs.subject s
        JOIN FETCH cs.teacher t
        JOIN FETCH l.startSlot ss
        JOIN FETCH l.endSlot es
        WHERE ar.student = :student
        AND cs.semester.id = :semesterId
        ORDER BY l.lessonDate DESC, ss.slotNumber
        """)
    List<AttendanceRecord> findByStudentAndSemester(
            @Param("student") Student student,
            @Param("semesterId") Long semesterId
    );

    @Query("SELECT ar FROM AttendanceRecord ar JOIN FETCH ar.student WHERE ar.lesson = :lesson ORDER BY ar.student.fullName")
    List<AttendanceRecord> findByLesson(@Param("lesson") Lesson lesson);

    Optional<AttendanceRecord> findByLessonAndStudent(Lesson lesson, Student student);
}
