# MyFPTSchool — Cổng Thông Tin Học Đường FPT THPT

Hệ thống quản lý học đường cho trường THPT FPT: quản lý học sinh, giáo viên, phụ huynh, thời khóa biểu, điểm danh, điểm số và thông báo.

---

## Tech Stack

| Layer | Công nghệ |
|---|---|
| Backend | Spring Boot 3.x · Java 21 · PostgreSQL · Flyway · Spring Security JWT |
| Frontend Admin | React 18 · TypeScript · Vite · TanStack Query · React Router v6 · Tailwind CSS · Zod · React Hook Form |
| Mobile (riêng) | Flutter (repo mobile) |

---

## Cấu trúc monorepo

```
myfptschool/
├── myfptschool-be/          Spring Boot backend
│   └── src/main/java/.../
│       ├── auth/            Xác thực JWT (login, refresh, logout)
│       ├── academic/        Lớp học, môn học, học kỳ, phân công (classroom-subjects)
│       ├── student/         Quản lý học sinh + import Excel
│       ├── teacher/         Quản lý giáo viên
│       ├── parent/          Quản lý phụ huynh + liên kết con
│       ├── timetable/       Tiết học, phòng, time slot
│       ├── attendance/      Điểm danh
│       ├── grade/           Điểm số
│       ├── notification/    Thông báo
│       ├── me/              API cho student/parent (profile, TKB, điểm, điểm danh)
│       ├── common/          BaseEntity, ApiResponse, AppException, ErrorCode
│       ├── security/        JWT filter, UserDetails, SecurityConfig
│       └── config/          AppConfig, OpenApiConfig (Swagger)
│
└── myfptschool-web/         React admin web
    └── src/
        ├── app/             Router, ProtectedRoute, Providers
        ├── features/        Trang theo tính năng (xem bên dưới)
        └── shared/          Components UI, hooks, lib (api, queryKeys), types
```

---

## Cài đặt & Chạy

### Yêu cầu
- Java 21, Maven 3.9+
- PostgreSQL 15+
- Node.js 18+, npm

### Backend

```bash
# 1. Tạo database PostgreSQL
createdb myfptschool

# 2. Cấu hình kết nối (src/main/resources/application.yml)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myfptschool
    username: <user>
    password: <password>

# 3. Chạy (Flyway tự migrate schema)
cd myfptschool-be
mvn spring-boot:run
```

Backend chạy tại `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

> **Lưu ý:** `ddl-auto: validate` — Hibernate chỉ validate, không tạo bảng. Schema do Flyway quản lý trong `src/main/resources/db/migration/`.

### Frontend Admin

```bash
cd myfptschool-web
npm install
npm run dev
```

Frontend chạy tại `http://localhost:5173`
Vite proxy `/api` → `http://localhost:8080` (cấu hình trong `vite.config.ts`)

---

## Tài khoản mặc định (seed data)

| Role | Username | Password |
|---|---|---|
| ADMIN | admin | Admin@123 |
| TEACHER | (tạo qua admin UI) | Teacher@123 |
| STUDENT | (tạo/import qua admin) | Student@123 |
| PARENT | (tạo/import qua admin) | Parent@123 |

---

## API Reference

Tất cả response có dạng `{ code, message, data }` (trừ binary download và một số Me endpoint).
Auth: `Authorization: Bearer <token>` header.

### Auth — `/api/v1/auth`
| Method | Path | Mô tả |
|---|---|---|
| POST | `/login` | Đăng nhập → trả về `accessToken`, `refreshToken` |
| POST | `/refresh` | Gia hạn access token |
| POST | `/logout` | Thu hồi refresh token |

### Admin — Students `/api/v1/admin`
| Method | Path | Role | Mô tả |
|---|---|---|---|
| GET | `/students` | ADMIN, TEACHER | Danh sách HS (search, page, size) |
| GET | `/students/{id}` | ADMIN, TEACHER | Chi tiết 1 HS |
| POST | `/students` | ADMIN | Tạo HS mới (tạo kèm tài khoản) |
| PUT | `/students/{id}` | ADMIN | Cập nhật thông tin HS |
| GET | `/students/import/template` | ADMIN | Tải file Excel mẫu |
| POST | `/students/import` | ADMIN | Import hàng loạt từ Excel (all-or-nothing) |

### Admin — Teachers
| Method | Path | Role | Mô tả |
|---|---|---|---|
| GET | `/teachers` | ADMIN, TEACHER | Danh sách GV (search, page, size) |
| GET | `/teachers/{id}` | ADMIN, TEACHER | Chi tiết 1 GV |
| POST | `/teachers` | ADMIN | Tạo GV mới |
| PUT | `/teachers/{id}` | ADMIN | Cập nhật thông tin GV |

### Admin — Parents
| Method | Path | Role | Mô tả |
|---|---|---|---|
| GET | `/parents` | ADMIN, TEACHER | Danh sách PH (search, page, size) |
| GET | `/parents/{id}` | ADMIN, TEACHER | Chi tiết 1 PH + danh sách con |
| POST | `/parents` | ADMIN | Tạo PH mới |
| PUT | `/parents/{id}` | ADMIN | Cập nhật thông tin PH |
| POST | `/students/{sId}/parents/{pId}` | ADMIN | Liên kết HS với PH |

### Admin — Academic
| Method | Path | Role | Mô tả |
|---|---|---|---|
| GET | `/campuses` | ADMIN, TEACHER | Danh sách cơ sở |
| GET | `/classrooms` | ADMIN, TEACHER | Danh sách lớp (có studentCount) |
| GET | `/classrooms/{id}/students` | ADMIN, TEACHER | HS trong 1 lớp (page, size) |
| GET | `/subjects` | ADMIN, TEACHER | Danh sách môn học |
| GET | `/semesters` | ADMIN, TEACHER | Danh sách học kỳ |
| GET | `/classroom-subjects` | ADMIN, TEACHER | Danh sách phân công (filter classroomId, semesterId) |
| POST | `/classroom-subjects` | ADMIN | Tạo phân công mới |

### Admin — Timetable & Lessons
| Method | Path | Role | Mô tả |
|---|---|---|---|
| GET | `/time-slots` | ADMIN, TEACHER | Danh sách tiết trong ngày |
| GET | `/rooms` | ADMIN, TEACHER | Danh sách phòng học |
| GET | `/classroom-subjects/{id}/lessons` | ADMIN, TEACHER | Danh sách tiết học |
| POST | `/classroom-subjects/{id}/lessons` | ADMIN, TEACHER | Tạo tiết học mới |
| PATCH | `/lessons/{id}` | ADMIN, TEACHER | Cập nhật trạng thái/phòng/ghi chú tiết học |

### Admin — Attendance & Grades
| Method | Path | Role | Mô tả |
|---|---|---|---|
| GET | `/lessons/{id}/attendance` | ADMIN, TEACHER | Lấy điểm danh theo tiết |
| POST | `/lessons/{id}/attendance` | ADMIN, TEACHER | Điểm danh / cập nhật |
| GET | `/classroom-subjects/{id}/grades` | ADMIN, TEACHER | Bảng điểm theo môn/lớp |
| POST | `/classroom-subjects/{id}/grades` | ADMIN, TEACHER | Nhập/cập nhật điểm hàng loạt |
| PUT | `/grades/{id}` | ADMIN, TEACHER | Sửa điểm 1 bản ghi |

### Admin — Notifications
| Method | Path | Role | Mô tả |
|---|---|---|---|
| POST | `/notifications` | ADMIN, TEACHER | Gửi thông báo (individual/classroom/all) |

### Me — Student & Parent `/api/v1/me`
| Method | Path | Role | Mô tả |
|---|---|---|---|
| GET | `/profile` | STUDENT, PARENT | Hồ sơ cá nhân |
| GET | `/timetable?date=&studentId=` | STUDENT, PARENT | TKB theo ngày |
| GET | `/grades?semesterId=&studentId=` | STUDENT, PARENT | Điểm số theo học kỳ |
| GET | `/attendance?semesterId=&studentId=` | STUDENT, PARENT | Tổng hợp điểm danh |
| GET | `/notifications` | ALL | Danh sách thông báo (page, size) |
| PUT | `/notifications/{id}/read` | ALL | Đánh dấu đã đọc |

> `/me/*` profile, timetable, grades, attendance — dành cho mobile Flutter app. Admin web không dùng các endpoint này.

---

## Frontend Admin — Danh sách trang

| Route | Component | Tính năng |
|---|---|---|
| `/login` | LoginPage | Đăng nhập JWT |
| `/dashboard` | DashboardPage | Thống kê: tổng HS, GV, lớp, môn |
| `/students` | StudentListPage | Danh sách HS · search · phân trang · **Tải template** · **Import Excel** |
| `/students/new` | StudentFormPage | Tạo HS mới |
| `/students/:id/edit` | StudentFormPage | Sửa thông tin HS |
| `/teachers` | TeacherListPage | Danh sách GV · search · phân trang |
| `/teachers/new` | TeacherFormPage | Tạo GV mới |
| `/teachers/:id/edit` | TeacherFormPage | Sửa thông tin GV |
| `/parents` | ParentListPage | Danh sách PH · search · phân trang |
| `/parents/new` | ParentFormPage | Tạo PH mới |
| `/parents/:id/edit` | ParentFormPage | Sửa PH · liên kết HS |
| `/classrooms` | ClassroomListPage | Danh sách 30 lớp theo khối · click xem HS |
| `/assignments` | AssignmentPage | Xem + tạo phân công môn/lớp |
| `/timetable` | TimetablePage | TKB · tạo tiết · đánh dấu đã học/hủy |
| `/attendance` | AttendancePage | Điểm danh theo tiết |
| `/grades` | GradesPage | Nhập/sửa bảng điểm |
| `/notifications` | NotificationListPage | Thông báo đã nhận · đánh dấu đọc |
| `/notifications/new` | NotificationComposePage | Soạn & gửi thông báo |

---

## Kiến trúc & Quy ước

### Backend
- **Package-by-feature**: mỗi domain là 1 package riêng (`entity`, `dto`, `repository`, `service`, `controller`)
- **BaseEntity**: `id (Long BIGSERIAL)`, `createdAt`, `updatedAt` (`insertable=false, updatable=false`)
- **Enum**: `@Enumerated(EnumType.STRING)`, tên enum chữ thường
- **Security**: JWT filter → `@PreAuthorize` method-level, role prefix `ROLE_`
- **Exception**: `AppException(ErrorCode)` hoặc `AppException(ErrorCode, String message)` → `GlobalExceptionHandler`
- **Schema**: Flyway migrate, Hibernate `ddl-auto: validate`

### Frontend
- **API helper**: `apiGet / apiPost / apiPut / apiPatch / apiDelete / apiDownload / apiUpload` trong `shared/lib/api.ts`
- **Auth**: token trong `localStorage`, interceptor 401 → redirect `/login`
- **Query cache**: TanStack Query, key factory trong `shared/lib/queryKeys.ts`
- **Form**: React Hook Form + `zodResolver(schema) as any` (fix type mismatch với `z.coerce.number()`)
- **Vite proxy**: `/api` → `http://localhost:8080`

---

## Lịch sử PR đã merge

| PR | Branch | Nội dung |
|---|---|---|
| #15 | feat/admin-lessons | Time slots API, admin lesson CRUD |
| #16 | feat/admin-enhancements | Campus API, teacher search, parent list, frontend toàn bộ |
| #17 | feat/admin-notifications | Gửi thông báo admin + me/notifications |
| #18 | feat/admin-student-list | **GET /admin/students** + StudentAdminResponse |
| #19 | fix/classroom-list-response-type | Fix ClassroomListPage dùng array thay PageResponse |
| #20 | feat/missing-integrations | Import Excel UI + ClassroomListPage xem HS theo lớp |

---

## Việc còn lại / Cải thiện có thể làm

### Còn thiếu (nên làm)
1. **Logout gọi API** — hiện tại frontend chỉ xóa `localStorage`, chưa gọi `POST /auth/logout` để thu hồi refresh token phía server
2. **Auto refresh token** — khi access token hết hạn, frontend redirect login thay vì tự refresh qua `POST /auth/refresh`

### Tính năng nâng cao (tùy chọn)
3. **Kích hoạt / khóa tài khoản** — backend có `User.activate()` / `User.deactivate()` nhưng chưa có API endpoint + UI
4. **Đổi mật khẩu** — chưa có cả backend lẫn frontend
5. **Xóa phân công** (`DELETE /admin/classroom-subjects/{id}`) — chưa có backend + UI
6. **Xóa tiết học** — chưa có endpoint DELETE
7. **Tìm kiếm học sinh theo lớp** trong ClassroomListPage — hiện chỉ load tất cả, chưa có search/filter theo tên trong lớp
8. **Dashboard nâng cao** — thống kê điểm danh %, điểm trung bình theo lớp
9. **Export điểm** — xuất bảng điểm ra Excel

### Mobile (Flutter — repo riêng)
- Các API `/me/profile`, `/me/timetable`, `/me/grades`, `/me/attendance` đã sẵn sàng nhưng chưa tích hợp vào Flutter app
