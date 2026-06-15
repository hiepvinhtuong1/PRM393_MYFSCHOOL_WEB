package vn.edu.fpt.myfptschool.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "semesters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Semester extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public static Semester create(AcademicYear academicYear, String name, LocalDate startDate, LocalDate endDate) {
        Semester s = new Semester();
        s.academicYear = academicYear;
        s.name = name;
        s.startDate = startDate;
        s.endDate = endDate;
        return s;
    }

    public void update(AcademicYear academicYear, String name, LocalDate startDate, LocalDate endDate) {
        this.academicYear = academicYear;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
