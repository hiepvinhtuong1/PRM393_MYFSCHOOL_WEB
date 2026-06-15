package vn.edu.fpt.myfptschool.academic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;

import java.util.List;
import java.util.Optional;

public interface ClassroomSubjectRepository extends JpaRepository<ClassroomSubject, Long> {

    @Query("SELECT cs FROM ClassroomSubject cs JOIN FETCH cs.teacher JOIN FETCH cs.subject JOIN FETCH cs.semester WHERE cs.classroom.id = :classroomId")
    List<ClassroomSubject> findAllByClassroomId(@Param("classroomId") Long classroomId);

    boolean existsByClassroomIdAndSubjectIdAndSemesterId(Long classroomId, Long subjectId, Long semesterId);

    @Query("SELECT cs FROM ClassroomSubject cs JOIN FETCH cs.classroom JOIN FETCH cs.subject JOIN FETCH cs.teacher WHERE cs.id = :id")
    Optional<ClassroomSubject> findByIdWithDetails(@Param("id") Long id);

    @Query(value = """
            SELECT cs FROM ClassroomSubject cs
            JOIN FETCH cs.classroom
            JOIN FETCH cs.subject
            JOIN FETCH cs.teacher
            JOIN FETCH cs.semester
            WHERE (:classroomId IS NULL OR cs.classroom.id = :classroomId)
              AND (:semesterId  IS NULL OR cs.semester.id  = :semesterId)
            """,
           countQuery = """
            SELECT COUNT(cs) FROM ClassroomSubject cs
            WHERE (:classroomId IS NULL OR cs.classroom.id = :classroomId)
              AND (:semesterId  IS NULL OR cs.semester.id  = :semesterId)
            """)
    Page<ClassroomSubject> findWithFilters(
            @Param("classroomId") Long classroomId,
            @Param("semesterId")  Long semesterId,
            Pageable pageable);
}
