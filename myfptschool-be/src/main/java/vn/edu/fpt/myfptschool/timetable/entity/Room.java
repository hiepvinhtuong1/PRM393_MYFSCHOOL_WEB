package vn.edu.fpt.myfptschool.timetable.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.academic.entity.Campus;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    public static Room create(String code, Campus campus) {
        Room r = new Room();
        r.code = code;
        r.campus = campus;
        return r;
    }
}
