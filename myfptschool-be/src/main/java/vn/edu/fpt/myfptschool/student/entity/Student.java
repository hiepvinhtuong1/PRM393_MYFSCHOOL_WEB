package vn.edu.fpt.myfptschool.student.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "student_code", nullable = false, unique = true, length = 20)
    private String studentCode;

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

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    public static Student create(User user, String studentCode, String fullName,
                                 LocalDate dateOfBirth, String gender, String phone,
                                 String email, String photoUrl, Classroom classroom) {
        Student s = new Student();
        s.user = user;
        s.studentCode = studentCode;
        s.fullName = fullName;
        s.dateOfBirth = dateOfBirth;
        s.gender = gender;
        s.phone = phone;
        s.email = email;
        s.photoUrl = photoUrl;
        s.classroom = classroom;
        return s;
    }

    public void update(String fullName, LocalDate dateOfBirth, String gender,
                       String phone, String email, String photoUrl, Classroom classroom) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.photoUrl = photoUrl;
        this.classroom = classroom;
    }
}
