package vn.edu.fpt.myfptschool.parent.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;
import vn.edu.fpt.myfptschool.student.entity.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parent extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "parent_code", nullable = false, unique = true, length = 20)
    private String parentCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "parent_students",
            joinColumns = @JoinColumn(name = "parent_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> children = new ArrayList<>();

    public static Parent create(User user, String parentCode, String fullName,
                                LocalDate dateOfBirth, String gender, String phone, String email) {
        Parent p = new Parent();
        p.user = user;
        p.parentCode = parentCode;
        p.fullName = fullName;
        p.dateOfBirth = dateOfBirth;
        p.gender = gender;
        p.phone = phone;
        p.email = email;
        return p;
    }

    public void update(String fullName, LocalDate dateOfBirth, String gender, String phone, String email) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
    }

    public void addChild(Student student) {
        children.add(student);
    }

    public void removeChild(Student student) {
        children.removeIf(c -> c.getId().equals(student.getId()));
    }
}
