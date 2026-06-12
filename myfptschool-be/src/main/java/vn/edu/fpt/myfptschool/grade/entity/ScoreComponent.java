package vn.edu.fpt.myfptschool.grade.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_components")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreComponent {

    @Id
    @Column(name = "id")
    private Short id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Short weight;

    @Column(name = "display_order", nullable = false)
    private Short displayOrder;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
