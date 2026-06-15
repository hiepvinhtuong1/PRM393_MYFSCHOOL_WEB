package vn.edu.fpt.myfptschool.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.attendance.dto.*;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceRecord;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceStatus;
import vn.edu.fpt.myfptschool.attendance.repository.AttendanceRecordRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.student.entity.Student;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;
import vn.edu.fpt.myfptschool.timetable.entity.Lesson;
import vn.edu.fpt.myfptschool.timetable.repository.LessonRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAttendanceService {

    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    @Transactional(readOnly = true)
    public LessonAttendanceResponse getAttendance(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdWithDetails(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tiết học không tồn tại"));

        var classroom = lesson.getClassroomSubject().getClassroom();
        List<Student> students = studentRepository.findByClassroomOrderByFullName(classroom);

        List<AttendanceRecord> records = attendanceRecordRepository.findByLesson(lesson);
        Map<Long, AttendanceRecord> recordByStudentId = records.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r));

        List<StudentAttendanceEntry> entries = students.stream()
                .map(s -> {
                    AttendanceRecord record = recordByStudentId.get(s.getId());
                    if (record != null) {
                        return new StudentAttendanceEntry(
                                s.getId(), s.getStudentCode(), s.getFullName(),
                                record.getStatus(), record.getNote(), true
                        );
                    } else {
                        return new StudentAttendanceEntry(
                                s.getId(), s.getStudentCode(), s.getFullName(),
                                null, null, false
                        );
                    }
                })
                .toList();

        long totalPresent = entries.stream()
                .filter(e -> e.status() == AttendanceStatus.present || e.status() == AttendanceStatus.late)
                .count();
        long totalAbsent = entries.stream()
                .filter(e -> e.status() == AttendanceStatus.excused_absent || e.status() == AttendanceStatus.unexcused_absent)
                .count();
        long totalUnrecorded = entries.stream().filter(e -> !e.recorded()).count();

        var cs = lesson.getClassroomSubject();
        return new LessonAttendanceResponse(
                lesson.getId(),
                lesson.getLessonDate().toString(),
                cs.getSubject().getName(),
                classroom.getName(),
                cs.getTeacher().getFullName(),
                lesson.getStartSlot().getStartTime().toString(),
                lesson.getEndSlot().getEndTime().toString(),
                lesson.getStatus().name(),
                students.size(),
                totalPresent,
                totalAbsent,
                totalUnrecorded,
                entries
        );
    }

    @Transactional
    public void submitAttendance(Long lessonId, SubmitAttendanceRequest request) {
        Lesson lesson = lessonRepository.findByIdWithDetails(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tiết học không tồn tại"));

        if (lesson.getLessonDate().isAfter(LocalDate.now())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Không thể điểm danh tiết học chưa diễn ra");
        }

        var classroom = lesson.getClassroomSubject().getClassroom();
        List<Student> students = studentRepository.findByClassroomOrderByFullName(classroom);
        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<AttendanceRecord> toSave = new ArrayList<>();

        for (AttendanceEntryRequest entry : request.entries()) {
            Student student = studentMap.get(entry.studentId());
            if (student == null) {
                throw new AppException(ErrorCode.NOT_FOUND,
                        "Học sinh ID " + entry.studentId() + " không thuộc lớp này");
            }

            AttendanceRecord record = attendanceRecordRepository
                    .findByLessonAndStudent(lesson, student)
                    .orElse(null);

            if (record == null) {
                toSave.add(AttendanceRecord.create(student, lesson, entry.status(), entry.note()));
            } else {
                record.update(entry.status(), entry.note());
                toSave.add(record);
            }
        }

        attendanceRecordRepository.saveAll(toSave);
    }
}
