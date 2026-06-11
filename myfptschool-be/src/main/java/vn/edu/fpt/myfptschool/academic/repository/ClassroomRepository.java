package vn.edu.fpt.myfptschool.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
}
