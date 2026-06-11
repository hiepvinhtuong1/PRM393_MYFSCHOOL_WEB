package vn.edu.fpt.myfptschool.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.academic.entity.AcademicYear;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
}
