package vn.edu.fpt.myfptschool.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;

public interface ClassroomSubjectRepository extends JpaRepository<ClassroomSubject, Long> {
}
