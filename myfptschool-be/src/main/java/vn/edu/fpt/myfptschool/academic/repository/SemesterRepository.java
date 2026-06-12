package vn.edu.fpt.myfptschool.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.fpt.myfptschool.academic.entity.Semester;

import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    @Query("SELECT s FROM Semester s JOIN FETCH s.academicYear ORDER BY s.startDate DESC")
    List<Semester> findAllWithAcademicYear();
}
