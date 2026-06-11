package vn.edu.fpt.myfptschool.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.academic.entity.Semester;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
}
