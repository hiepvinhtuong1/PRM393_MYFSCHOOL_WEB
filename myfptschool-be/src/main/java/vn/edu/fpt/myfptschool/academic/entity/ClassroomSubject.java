package vn.edu.fpt.myfptschool.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;
import vn.edu.fpt.myfptschool.teacher.entity.Teacher;

@Entity
@Table(name = "classroom_subjects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassroomSubject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    public static ClassroomSubject create(Classroom classroom, Subject subject, Teacher teacher, Semester semester) {
        ClassroomSubject cs = new ClassroomSubject();
        cs.classroom = classroom;
        cs.subject = subject;
        cs.teacher = teacher;
        cs.semester = semester;
        return cs;
    }
}
