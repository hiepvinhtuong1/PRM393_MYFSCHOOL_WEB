package vn.edu.fpt.myfptschool.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.dto.*;
import vn.edu.fpt.myfptschool.academic.entity.*;
import vn.edu.fpt.myfptschool.academic.repository.*;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.grade.repository.GradeRecordRepository;
import vn.edu.fpt.myfptschool.teacher.entity.Teacher;
import vn.edu.fpt.myfptschool.teacher.repository.TeacherRepository;
import vn.edu.fpt.myfptschool.timetable.repository.LessonRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminClassroomSubjectService {

    private final ClassroomSubjectRepository classroomSubjectRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SemesterRepository semesterRepository;
    private final AcademicYearRepository academicYearRepository;
    private final LessonRepository lessonRepository;
    private final GradeRecordRepository gradeRecordRepository;

    @Transactional(readOnly = true)
    public ClassroomSubjectPageResponse getClassroomSubjects(Long classroomId, Long semesterId, int page, int size) {
        Page<ClassroomSubject> result = classroomSubjectRepository
                .findWithFilters(classroomId, semesterId, PageRequest.of(page, size));
        return new ClassroomSubjectPageResponse(
                result.getContent().stream().map(ClassroomSubjectResponse::from).toList(),
                result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages()
        );
    }

    @Transactional
    public ClassroomSubjectResponse createClassroomSubject(CreateClassroomSubjectRequest req) {
        Classroom classroom = classroomRepository.findById(req.classroomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Subject subject = subjectRepository.findById(req.subjectId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Teacher teacher = teacherRepository.findById(req.teacherId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Semester semester = semesterRepository.findById(req.semesterId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (classroomSubjectRepository.existsByClassroomIdAndSubjectIdAndSemesterId(
                req.classroomId(), req.subjectId(), req.semesterId())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Lớp " + classroom.getName() + " đã có môn " + subject.getName()
                            + " trong học kỳ này");
        }

        ClassroomSubject cs = classroomSubjectRepository.save(
                ClassroomSubject.create(classroom, subject, teacher, semester));

        return ClassroomSubjectResponse.from(cs);
    }

    @Transactional
    public void deleteClassroomSubject(Long id) {
        ClassroomSubject cs = classroomSubjectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Phân công không tồn tại"));
        if (!lessonRepository.findByClassroomSubject(cs).isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Xóa tất cả tiết học của phân công này trước");
        }
        if (gradeRecordRepository.existsByClassroomSubject(cs)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Xóa tất cả điểm số của phân công này trước");
        }
        classroomSubjectRepository.delete(cs);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream().map(SubjectResponse::from).toList();
    }

    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest req) {
        Subject subject = subjectRepository.save(
                Subject.create(req.name(), req.colorHex(), req.coefficient()));
        return SubjectResponse.from(subject);
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, UpdateSubjectRequest req) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Môn học không tồn tại"));
        subject.update(req.name(), req.colorHex(), req.coefficient());
        return SubjectResponse.from(subjectRepository.save(subject));
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getAllSemesters() {
        return semesterRepository.findAllWithAcademicYear().stream().map(SemesterResponse::from).toList();
    }

    @Transactional
    public SemesterResponse createSemester(CreateSemesterRequest req) {
        AcademicYear academicYear = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Năm học không tồn tại"));
        LocalDate start = LocalDate.parse(req.startDate());
        LocalDate end = LocalDate.parse(req.endDate());
        Semester semester = semesterRepository.save(Semester.create(academicYear, req.name(), start, end));
        return SemesterResponse.from(semester);
    }

    @Transactional
    public SemesterResponse updateSemester(Long id, UpdateSemesterRequest req) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Học kỳ không tồn tại"));
        AcademicYear academicYear = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Năm học không tồn tại"));
        LocalDate start = LocalDate.parse(req.startDate());
        LocalDate end = LocalDate.parse(req.endDate());
        semester.update(academicYear, req.name(), start, end);
        return SemesterResponse.from(semesterRepository.save(semester));
    }

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAcademicYears() {
        return academicYearRepository.findAll().stream().map(AcademicYearResponse::from).toList();
    }
}
