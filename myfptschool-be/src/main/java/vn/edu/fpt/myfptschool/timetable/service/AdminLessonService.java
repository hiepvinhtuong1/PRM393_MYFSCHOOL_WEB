package vn.edu.fpt.myfptschool.timetable.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;
import vn.edu.fpt.myfptschool.academic.repository.ClassroomSubjectRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.timetable.dto.AdminLessonResponse;
import vn.edu.fpt.myfptschool.timetable.dto.CreateLessonRequest;
import vn.edu.fpt.myfptschool.timetable.dto.RoomResponse;
import vn.edu.fpt.myfptschool.timetable.dto.UpdateLessonRequest;
import vn.edu.fpt.myfptschool.timetable.entity.Lesson;
import vn.edu.fpt.myfptschool.timetable.entity.Room;
import vn.edu.fpt.myfptschool.timetable.entity.TimeSlot;
import vn.edu.fpt.myfptschool.timetable.repository.LessonRepository;
import vn.edu.fpt.myfptschool.timetable.repository.RoomRepository;
import vn.edu.fpt.myfptschool.timetable.repository.TimeSlotRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminLessonService {

    private final ClassroomSubjectRepository classroomSubjectRepository;
    private final LessonRepository lessonRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final RoomRepository roomRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional(readOnly = true)
    public List<AdminLessonResponse> getLessons(Long classroomSubjectId) {
        ClassroomSubject cs = classroomSubjectRepository.findById(classroomSubjectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Môn học lớp không tồn tại"));
        return lessonRepository.findByClassroomSubjectOrdered(cs)
                .stream().map(AdminLessonResponse::from).toList();
    }

    @Transactional
    public AdminLessonResponse createLesson(Long classroomSubjectId, CreateLessonRequest request) {
        ClassroomSubject cs = classroomSubjectRepository.findByIdWithDetails(classroomSubjectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Môn học lớp không tồn tại"));

        LocalDate date;
        try {
            date = LocalDate.parse(request.lessonDate(), DATE_FMT);
        } catch (Exception e) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Ngày không hợp lệ: " + request.lessonDate());
        }

        TimeSlot startSlot = timeSlotRepository.findById(request.startSlotId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tiết bắt đầu không tồn tại"));
        TimeSlot endSlot = timeSlotRepository.findById(request.endSlotId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tiết kết thúc không tồn tại"));

        if (endSlot.getSlotNumber() < startSlot.getSlotNumber()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Tiết kết thúc phải >= tiết bắt đầu");
        }

        Room room = null;
        if (request.roomId() != null) {
            room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Phòng học không tồn tại"));
        }

        Lesson lesson = lessonRepository.save(Lesson.create(cs, date, startSlot, endSlot, room));
        return AdminLessonResponse.from(lesson);
    }

    @Transactional
    public AdminLessonResponse updateLesson(Long lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findByIdWithDetails(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tiết học không tồn tại"));

        Room room = null;
        if (request.roomId() != null) {
            room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Phòng học không tồn tại"));
        }

        lesson.update(request.status(), room, request.note());
        lessonRepository.save(lesson);
        return AdminLessonResponse.from(lesson);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getRooms() {
        return roomRepository.findAll().stream().map(RoomResponse::from).toList();
    }
}
