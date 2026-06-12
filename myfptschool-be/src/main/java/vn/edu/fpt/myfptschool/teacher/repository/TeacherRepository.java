package vn.edu.fpt.myfptschool.teacher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.teacher.entity.Teacher;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @EntityGraph(attributePaths = {"campus", "user"})
    Page<Teacher> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"campus", "user"})
    Optional<Teacher> findById(Long id);

    boolean existsByUserId(Long userId);
}
