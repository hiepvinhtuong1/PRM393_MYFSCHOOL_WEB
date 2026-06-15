package vn.edu.fpt.myfptschool.parent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.parent.entity.Parent;

import java.util.List;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    @EntityGraph(attributePaths = {"user", "children"})
    Page<Parent> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "children"})
    Page<Parent> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Parent p LEFT JOIN FETCH p.children c LEFT JOIN FETCH c.classroom cl LEFT JOIN FETCH cl.campus WHERE p.user = :user")
    Optional<Parent> findByUserWithChildren(@Param("user") User user);

    @Query("SELECT p FROM Parent p LEFT JOIN FETCH p.children WHERE p.id = :id")
    Optional<Parent> findByIdWithChildren(@Param("id") Long id);

    boolean existsByParentCode(String parentCode);

    long countByChildrenId(Long studentId);

    boolean existsByIdAndChildrenId(Long parentId, Long studentId);

    Optional<Parent> findByParentCode(String parentCode);

    @Query("SELECT DISTINCT p.user FROM Parent p JOIN p.children s WHERE s.classroom = :classroom")
    List<User> findUsersByChildrenClassroom(@Param("classroom") Classroom classroom);
}
