package vn.edu.fpt.myfptschool.grade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;
import vn.edu.fpt.myfptschool.grade.entity.GradeRecord;
import vn.edu.fpt.myfptschool.grade.entity.ScoreComponent;
import vn.edu.fpt.myfptschool.student.entity.Student;

import java.util.List;
import java.util.Optional;

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

    @Query("""
        SELECT gr FROM GradeRecord gr
        JOIN FETCH gr.student s
        JOIN FETCH gr.component c
        WHERE gr.classroomSubject = :cs
        ORDER BY s.fullName, c.displayOrder
        """)
    List<GradeRecord> findByClassroomSubjectWithDetails(@Param("cs") ClassroomSubject cs);

    Optional<GradeRecord> findByClassroomSubjectAndStudentAndComponent(
            ClassroomSubject classroomSubject, Student student, ScoreComponent component);

    boolean existsByClassroomSubject(ClassroomSubject classroomSubject);
}
