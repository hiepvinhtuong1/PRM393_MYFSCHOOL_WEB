package vn.edu.fpt.myfptschool.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

@Entity
@Table(name = "campuses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campus extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String website;

    public static Campus create(String name, String address) {
        Campus c = new Campus();
        c.name = name;
        c.address = address;
        return c;
    }
}
