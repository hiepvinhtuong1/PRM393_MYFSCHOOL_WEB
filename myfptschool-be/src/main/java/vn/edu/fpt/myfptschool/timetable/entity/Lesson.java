package vn.edu.fpt.myfptschool.timetable.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "lessons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_subject_id", nullable = false)
    private ClassroomSubject classroomSubject;

    @Column(name = "lesson_date", nullable = false)
    private LocalDate lessonDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_slot_id", nullable = false)
    private TimeSlot startSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_slot_id", nullable = false)
    private TimeSlot endSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonStatus status;

    @Column(name = "has_materials", nullable = false)
    private boolean hasMaterials;

    private String note;

    public static Lesson create(ClassroomSubject classroomSubject, LocalDate lessonDate,
                                TimeSlot startSlot, TimeSlot endSlot, Room room) {
        Lesson l = new Lesson();
        l.classroomSubject = classroomSubject;
        l.lessonDate = lessonDate;
        l.startSlot = startSlot;
        l.endSlot = endSlot;
        l.room = room;
        l.status = LessonStatus.scheduled;
        l.hasMaterials = false;
        return l;
    }
}
