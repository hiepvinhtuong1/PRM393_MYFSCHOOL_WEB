package vn.edu.fpt.myfptschool.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

@Entity
@Table(name = "subjects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subject extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "sessions_per_semester")
    private Short sessionsPerSemester;

    @Column(nullable = false)
    private Short coefficient;

    public static Subject create(String name, String colorHex, int coefficient) {
        Subject s = new Subject();
        s.name = name;
        s.colorHex = colorHex;
        s.coefficient = (short) coefficient;
        return s;
    }
}
