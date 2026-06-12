package vn.edu.fpt.myfptschool.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    @Query("""
        SELECT cl FROM Classroom cl
        JOIN FETCH cl.campus
        JOIN FETCH cl.academicYear
        ORDER BY cl.gradeLevel, cl.name
        """)
    List<Classroom> findAllWithDetails();

    @Query("""
        SELECT cl FROM Classroom cl
        JOIN FETCH cl.campus
        JOIN FETCH cl.academicYear
        WHERE cl.id = :id
        """)
    Optional<Classroom> findByIdWithDetails(Long id);
}
