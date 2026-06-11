package vn.edu.fpt.myfptschool.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

@Entity
@Table(name = "classrooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Classroom extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String name;

    @Column(name = "grade_level", nullable = false)
    private Short gradeLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    public static Classroom create(String name, Short gradeLevel, Campus campus, AcademicYear academicYear) {
        Classroom cl = new Classroom();
        cl.name = name;
        cl.gradeLevel = gradeLevel;
        cl.campus = campus;
        cl.academicYear = academicYear;
        return cl;
    }
}
