# HRM - Human Resource Management System

Hệ thống quản trị nhân sự được xây dựng bằng Java Web, hỗ trợ quản lý người dùng, nhân viên, phòng ban, hợp đồng, đơn từ, chấm công và tiền lương theo từng vai trò trong doanh nghiệp.

## Chức năng chính

- Đăng nhập, phân quyền theo vai trò và ghi nhận nhật ký hoạt động.
- Quản lý tài khoản, vai trò và quyền truy cập.
- Quản lý hồ sơ nhân viên, phòng ban và chức vụ.
- Quản lý hợp đồng lao động, phê duyệt và xuất tài liệu PDF.
- Xử lý đơn nghỉ phép, tăng ca, khiếu nại, thuyên chuyển, thăng/giáng chức và đăng ký người phụ thuộc.
- Nhập dữ liệu chấm công từ Excel, theo dõi công và chốt kỳ chấm công.
- Cấu hình, tính toán, phê duyệt và theo dõi bảng lương.
- Phân tách màn hình nghiệp vụ cho System Admin, Business Admin, quản lý và nhân viên các phòng ban.

## Công nghệ sử dụng

- Java 17
- Jakarta EE 10 (Servlet, JSP, JSTL)
- Apache Tomcat 10.1
- MySQL 8
- Apache Ant và NetBeans
- Bootstrap 5
- MySQL Connector/J, Apache POI, PDFBox, OpenPDF, Gson và BCrypt

## Cấu trúc dự án

```text
SWP_GROUP2/
├── HRM/
│   ├── src/java/
│   │   ├── config/       # Khởi tạo ứng dụng và cấu hình
│   │   ├── controller/   # Servlet/controller theo vai trò
│   │   ├── dao/          # Truy cập dữ liệu
│   │   ├── dal/          # Kết nối và khởi tạo cơ sở dữ liệu
│   │   ├── dto/          # Đối tượng truyền dữ liệu
│   │   ├── model/        # Mô hình nghiệp vụ
│   │   ├── service/      # Xử lý nghiệp vụ
│   │   └── utils/        # Tiện ích dùng chung
│   ├── web/              # JSP, tài nguyên tĩnh và WEB-INF
│   ├── nbproject/        # Cấu hình dự án NetBeans
│   ├── build.xml         # Cấu hình build bằng Ant
│   ├── Dockerfile
│   └── docker-compose.yml
└── sql/                  # Script bổ sung và dữ liệu kiểm thử
```

## Yêu cầu môi trường

- JDK 17
- Apache Tomcat 10.1
- MySQL 8
- NetBeans 17 trở lên với hỗ trợ Jakarta EE; hoặc Apache Ant nếu build từ dòng lệnh

## Cài đặt và chạy dự án

### 1. Lấy mã nguồn

```bash
git clone https://github.com/nguyenlebinhan/SWP_GROUP2.git
cd SWP_GROUP2
```

### 2. Tạo cơ sở dữ liệu

Đăng nhập MySQL và tạo database rỗng:

```sql
CREATE DATABASE hrm
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

Ứng dụng tự tạo bảng, cập nhật cấu trúc và thêm dữ liệu mẫu khi khởi động lần đầu. Quá trình khởi động thông thường không xóa dữ liệu hiện có.

### 3. Cấu hình kết nối

Tạo file `HRM/src/java/config/.env` với nội dung:

```properties
db.url=jdbc:mysql://localhost:3306/hrm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
db.username=root
db.password=your_mysql_password
FILE_PART=attendanceFile
UPLOAD_DIR=uploads
```

File `.env` đã được loại khỏi Git. Không commit mật khẩu hoặc thông tin môi trường cá nhân vào repository.

Cấu hình cũng có thể được truyền qua biến môi trường `DB-URL`, `DB-USERNAME`, `DB-PASSWORD`, `FILE_PART` và `UPLOAD_DIR`; biến môi trường được ưu tiên hơn file cấu hình.

### 4. Chạy bằng NetBeans

1. Mở thư mục `HRM` dưới dạng project trong NetBeans.
2. Thêm Apache Tomcat 10.1 vào tab **Services > Servers**.
3. Chọn JDK 17 cho project.
4. Chạy **Clean and Build**, sau đó chọn **Run**.
5. Truy cập `http://localhost:8080/HRM/`.

Tài khoản System Admin mẫu dùng cho môi trường phát triển:

```text
Username: admin
Password: admin123
```

Hãy đổi hoặc vô hiệu hóa tài khoản mẫu trước khi triển khai vào môi trường thật.

### 5. Build WAR bằng Ant

Khi Ant và các thư viện Jakarta EE của NetBeans đã được cấu hình:

```bash
ant -f HRM/build.xml clean dist
```

File triển khai được tạo tại `HRM/dist/HRM.war`. Có thể chép file này vào thư mục `webapps` của Tomcat.

## Docker

`HRM/Dockerfile` triển khai file `dist/HRM.war` dưới context root `/`. Vì vậy cần build WAR trước khi build image:

> `docker-compose.yml` hiện khai báo image `mysql8.0`. Nếu môi trường của bạn không có image nội bộ với tên này, hãy đổi thành image chính thức `mysql:8.0` trước khi chạy.

```bash
cd HRM
docker compose up --build
```

Trước khi dùng Docker Compose, hãy kiểm tra lại image MySQL và thay thông tin đăng nhập mặc định trong `docker-compose.yml` cho phù hợp với môi trường triển khai.

## Lưu ý phát triển

- Không đưa `.env`, file build, log hoặc cấu hình IDE cá nhân vào Git.
- Các file thư viện runtime hiện được đặt trong `HRM/web/WEB-INF/lib`.
- Dữ liệu upload được lưu trong thư mục ứng dụng; cần cấu hình volume hoặc nơi lưu bền vững khi triển khai production.
- Các file Excel mẫu ở thư mục gốc và `HRM/web/downloads` có thể dùng để kiểm thử chức năng import.

## Giấy phép

Dự án phục vụ mục đích học tập của SWP Group 2. Chưa có giấy phép mã nguồn mở riêng đi kèm repository.
