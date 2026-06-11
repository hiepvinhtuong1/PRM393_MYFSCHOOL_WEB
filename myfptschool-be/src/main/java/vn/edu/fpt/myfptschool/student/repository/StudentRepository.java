package vn.edu.fpt.myfptschool.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.student.entity.Student;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.classroom cl LEFT JOIN FETCH cl.campus WHERE s.user = :user")
    Optional<Student> findByUserWithClassroom(@Param("user") User user);
}
