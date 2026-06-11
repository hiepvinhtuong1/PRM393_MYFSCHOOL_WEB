package vn.edu.fpt.myfptschool.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "academic_years")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicYear extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String label;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public static AcademicYear create(String label, LocalDate startDate, LocalDate endDate) {
        AcademicYear ay = new AcademicYear();
        ay.label = label;
        ay.startDate = startDate;
        ay.endDate = endDate;
        return ay;
    }
}
