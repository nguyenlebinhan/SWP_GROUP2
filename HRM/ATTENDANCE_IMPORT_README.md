# Import chấm công từ Excel (.xlsx)

Tính năng cho phép HR (HRManager / HREmployee) upload file Excel chấm công, hệ thống
đọc từng dòng, validate, map nhân viên theo `employeeCode` và lưu vào bảng `Attendance`,
đồng thời lưu metadata file vào `Uploaded_Files`.

## 1. Dependency BẮT BUỘC phải thêm (Apache POI)

Hiện `web/WEB-INF/lib` CHƯA có Apache POI nên project **sẽ không build/deploy được**
cho tới khi copy đủ các jar sau vào `web/WEB-INF/lib` (và Add JAR/Folder trong NetBeans
> Project > Libraries). Phiên bản POI 5.2.x đã kiểm thử:

```
poi-5.2.5.jar
poi-ooxml-5.2.5.jar
poi-ooxml-lite-5.2.5.jar
xmlbeans-5.2.0.jar
commons-collections4-4.4.jar
commons-compress-1.26.0.jar
commons-io-2.15.1.jar
SparseBitSet-1.3.jar
log4j-api-2.21.1.jar
```

Tải nhanh: trang Apache POI "Download" (poi-bin-5.2.5-...zip) đã chứa toàn bộ các jar
phụ thuộc ở trên trong thư mục `lib/` và `ooxml-lib/`.

> Lỗi điển hình khi thiếu jar: `package org.apache.poi.ss.usermodel does not exist`,
> `cannot find symbol XSSFWorkbook` (thiếu poi-ooxml), hoặc khi chạy:
> `NoClassDefFoundError: org/apache/logging/log4j/LogManager` (thiếu log4j-api),
> `NoClassDefFoundError: com/zaxxer/sparsebits/SparseBitSet` (thiếu SparseBitSet).

## 2. Định dạng file Excel

- Sheet **đầu tiên**, **dòng 1 là header**, đúng thứ tự cột:

| employeeCode | fullName     | Phòng ban       | workDate   | timeIn | timeOut | attendanceStatus | note |
|--------------|--------------|-----------------|------------|--------|---------|------------------|------|
| EMP001       | Nguyễn Văn A | Phòng Kỹ thuật  | 2026-06-01 | 08:00  | 17:00   | PRESENT          |      |
| EMP002       | Trần Thị B   | Phòng Kỹ thuật  | 2026-06-01 | 08:20  | 17:00   | LATE             |      |
| EMP003       | Lê Văn C     | Phòng Kỹ thuật  | 2026-06-01 |        |         | ABSENT           |      |

- `fullName` và `Phòng ban` được lưu thẳng (snapshot) vào bảng `Attendance` để khi xem danh sách không cần JOIN sang `Users`/`Departments`.

- `workDate`: `yyyy-MM-dd`. `timeIn`/`timeOut`: `HH:mm` (hoặc `HH:mm:ss`).
- `attendanceStatus` (map sang TINYINT): `PRESENT`→0, `LATE`→1, `ABSENT`→2, `UNEXCUSED`→3.
- `timeIn`/`timeOut` được phép để trống khi trạng thái là `ABSENT`/`UNEXCUSED`.

File mẫu: `attendance_import_sample.csv` (mở bằng Excel rồi **Save As → .xlsx**).

## 3. Quy tắc validate (mỗi dòng độc lập, dòng lỗi không làm hỏng cả file)

- File phải `.xlsx`, header đúng.
- `employeeCode` bắt buộc và phải tồn tại trong bảng `Employees`.
- `workDate` bắt buộc, parse được `java.sql.Date`.
- `timeIn`/`timeOut` nếu có phải parse được `java.sql.Time`; `timeOut` phải sau `timeIn`.
- `attendanceStatus` bắt buộc, thuộc tập hợp lệ.
- Nếu chọn import theo phòng ban, nhân viên phải thuộc phòng ban đó.
- `hoursWorked` = (timeOut − timeIn) tính theo giờ; ABSENT/UNEXCUSED = 0.

## 4. Phân quyền

- Permission mới `IMPORT_ATTENDANCE` (seed cho `HRManager`, `HREmployee`).
- Dùng `VIEW_ATTENDANCE` để xem dữ liệu (cũng được cấp cho HR).
- Chỉ user có role chứa `HR` + permission `IMPORT_ATTENDANCE` mới import được.
- Màn xem: HR xem toàn bộ; Manager chỉ phòng mình; Employee chỉ của bản thân.

## 5. URL

- `GET  /v1/attendance/import` — form import.
- `POST /v1/attendance/import` — xử lý upload/import (multipart, field file = `attendanceFile`, tối đa 10MB).
- `GET  /v1/attendance/list` — danh sách chấm công có lọc.

## 6. DB

- `DBInitializer` đã thêm cho bảng `Attendance`: cột `fileId INT NULL`,
  FK `fileId → Uploaded_Files(fileId)`, unique key `(employeeId, workDate)`.
- Khi import lại cùng employee + ngày: dùng `INSERT ... ON DUPLICATE KEY UPDATE` (không tạo dòng trùng).
- `attendanceCode` sinh dạng `ATT-EMP001-20260601`.
- File gốc lưu tại `web/uploads/attendance/` với tên do server sinh (chống path traversal).
