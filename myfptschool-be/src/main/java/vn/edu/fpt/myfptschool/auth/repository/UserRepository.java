package vn.edu.fpt.myfptschool.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.auth.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query(value = """
            SELECT u.* FROM users u WHERE u.id = (
                SELECT s.user_id FROM students s WHERE s.phone = :phone
                UNION ALL
                SELECT p.user_id FROM parents p WHERE p.phone = :phone
                UNION ALL
                SELECT t.user_id FROM teachers t WHERE t.phone = :phone
                LIMIT 1
            )
            """, nativeQuery = true)
    Optional<User> findByPhone(@Param("phone") String phone);
}
