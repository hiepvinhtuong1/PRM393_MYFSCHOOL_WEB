package vn.edu.fpt.myfptschool.grade.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;
import vn.edu.fpt.myfptschool.student.entity.Student;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grade_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_subject_id", nullable = false)
    private ClassroomSubject classroomSubject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private ScoreComponent component;

    @Column(precision = 4, scale = 2)
    private BigDecimal score;

    @Column(name = "recorded_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime recordedAt;

    public static GradeRecord create(Student student, ClassroomSubject classroomSubject,
                                     ScoreComponent component, BigDecimal score) {
        GradeRecord gr = new GradeRecord();
        gr.student = student;
        gr.classroomSubject = classroomSubject;
        gr.component = component;
        gr.score = score;
        return gr;
    }

    public void update(BigDecimal score) {
        this.score = score;
    }
}
