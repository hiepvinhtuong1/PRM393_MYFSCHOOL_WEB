package vn.edu.fpt.myfptschool.attendance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.timetable.entity.Lesson;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    private String note;

    @Column(name = "recorded_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime recordedAt;

    public static AttendanceRecord create(Student student, Lesson lesson, AttendanceStatus status) {
        AttendanceRecord ar = new AttendanceRecord();
        ar.student = student;
        ar.lesson = lesson;
        ar.status = status;
        return ar;
    }
}
