package vn.edu.fpt.myfptschool.grade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.grade.entity.GradeRecord;
import vn.edu.fpt.myfptschool.student.entity.Student;

import java.util.List;

public interface GradeRecordRepository extends JpaRepository<GradeRecord, Long> {

    @Query("""
        SELECT gr FROM GradeRecord gr
        JOIN FETCH gr.classroomSubject cs
        JOIN FETCH cs.subject s
        JOIN FETCH gr.component c
        WHERE gr.student = :student
        AND cs.semester.id = :semesterId
        ORDER BY s.name, c.displayOrder
        """)
    List<GradeRecord> findByStudentAndSemester(
            @Param("student") Student student,
            @Param("semesterId") Long semesterId
    );
}
