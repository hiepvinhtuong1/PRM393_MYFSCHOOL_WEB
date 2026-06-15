package vn.edu.fpt.myfptschool.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.student.entity.Student;

import java.util.List;
import java.util.Optional;


public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.classroom cl LEFT JOIN FETCH cl.campus WHERE s.user = :user")
    Optional<Student> findByUserWithClassroom(@Param("user") User user);

    Page<Student> findByClassroomOrderByFullName(Classroom classroom, Pageable pageable);

    List<Student> findByClassroomOrderByFullName(Classroom classroom);

    long countByClassroom(Classroom classroom);

    boolean existsByStudentCode(String studentCode);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.classroom cl LEFT JOIN FETCH cl.campus WHERE s.id = :id")
    Optional<Student> findByIdWithClassroom(@Param("id") Long id);

    @Query("SELECT s.user FROM Student s WHERE s.classroom = :classroom")
    List<User> findUsersByClassroom(@Param("classroom") Classroom classroom);
}
