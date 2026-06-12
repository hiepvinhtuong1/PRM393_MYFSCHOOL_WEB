package vn.edu.fpt.myfptschool.grade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.grade.entity.ScoreComponent;

import java.util.Optional;

public interface ScoreComponentRepository extends JpaRepository<ScoreComponent, Short> {
    Optional<ScoreComponent> findByCode(String code);
}
