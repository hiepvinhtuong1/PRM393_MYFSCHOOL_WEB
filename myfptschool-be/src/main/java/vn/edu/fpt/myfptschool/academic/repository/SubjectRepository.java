package vn.edu.fpt.myfptschool.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.academic.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
