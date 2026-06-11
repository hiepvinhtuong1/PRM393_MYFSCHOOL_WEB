package vn.edu.fpt.myfptschool.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.entity.UserSession;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByToken(String token);

    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.user = :user AND us.platform = :platform")
    void deleteByUserAndPlatform(@Param("user") User user, @Param("platform") String platform);
}
