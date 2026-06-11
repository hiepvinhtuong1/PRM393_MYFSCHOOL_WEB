package vn.edu.fpt.myfptschool.teacher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.teacher.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}
