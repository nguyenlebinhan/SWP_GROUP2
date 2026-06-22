# =============================================================================
#  Dockerfile cho dự án HRM (Java Web App chạy trên Tomcat)
#  Mục đích: học cách đóng gói 1 file .war thành 1 container chạy được
# =============================================================================
#
#  Mỗi dòng "lệnh" (FROM, COPY, EXPOSE...) gọi là 1 INSTRUCTION.
#  Docker đọc từ trên xuống dưới, mỗi instruction tạo ra 1 "layer" (lớp).
#
# -----------------------------------------------------------------------------
# 1) FROM: chọn image nền (base image) để bắt đầu xây dựng.
#    - tomcat:10.1-jdk17  = đã có sẵn Tomcat 10.1 + Java 17 bên trong.
#    - App của bạn dùng Jakarta EE 10 (gói jakarta.*) nên BẮT BUỘC Tomcat 10.x,
#      Tomcat 9 trở xuống dùng javax.* sẽ KHÔNG chạy được.
# -----------------------------------------------------------------------------
FROM tomcat:10.1-jdk17

# -----------------------------------------------------------------------------
# 2) LABEL: thông tin mô tả (không bắt buộc, chỉ để ghi chú metadata).
# -----------------------------------------------------------------------------
LABEL maintainer="HRM"
LABEL version="1.0"
LABEL description="Hệ thống quản lý nhân sự (HRM) - Java + Tomcat + MySQL"

# -----------------------------------------------------------------------------
# 3) Dọn sạch app mặc định của Tomcat.
#    Image tomcat chính thức để các app mẫu (docs, examples...) trong
#    thư mục 'webapps'. Mình xoá đi cho gọn và đỡ rủi ro bảo mật.
# -----------------------------------------------------------------------------
RUN rm -rf /usr/local/tomcat/webapps/*

# -----------------------------------------------------------------------------
# 4) COPY: chép file .war từ máy của bạn vào trong image.
#    - Nguồn: dist/HRM.war  (đường dẫn TƯƠNG ĐỐI so với "build context",
#      tức thư mục bạn chạy lệnh `docker build`, ở đây là thư mục HRM).
#    - Đích: đặt tên là ROOT.war để app chạy ở đường dẫn gốc "/"
#      (truy cập http://localhost:8080/ thay vì http://localhost:8080/HRM).
#      Nếu muốn giữ context path /HRM thì đổi thành ".../webapps/HRM.war".
# -----------------------------------------------------------------------------
COPY dist/HRM.war /usr/local/tomcat/webapps/ROOT.war

# -----------------------------------------------------------------------------
# 5) ENV: đặt biến môi trường mặc định cho cấu hình DB.
#    Code đọc cấu hình qua ConfigManager: nó lấy key "db.url" rồi đổi thành
#    biến môi trường tên "DB-URL" (viết hoa, thay '.' bằng '-').
#    => Vì vậy tên biến phải là DB-URL / DB-USERNAME / DB-PASSWORD.
#    Bạn có thể GHI ĐÈ các giá trị này khi chạy `docker run -e ...`,
#    trong docker-compose, hoặc bằng "Application settings" trên Azure.
#    KHÔNG nên hardcode mật khẩu thật ở đây — đây chỉ là default cho local.
# -----------------------------------------------------------------------------
ENV DB-URL="jdbc:mysql://mysql:3306/hrm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
ENV DB-USERNAME="root"
ENV DB-PASSWORD="root"

# -----------------------------------------------------------------------------
# 6) EXPOSE: khai báo container lắng nghe ở cổng 8080 (cổng mặc định Tomcat).
#    Lưu ý: EXPOSE chỉ mang tính "tài liệu hoá", muốn truy cập từ ngoài
#    vẫn phải map cổng khi chạy: `docker run -p 8080:8080 ...`
#
#    >>> AZURE App Service for Containers: mặc định Azure gửi request vào cổng
#        80. Vì Tomcat nghe cổng 8080 nên BẮT BUỘC đặt app setting
#        WEBSITES_PORT=8080 (xem AZURE-DEPLOY.md) để Azure route đúng cổng.
# -----------------------------------------------------------------------------
EXPOSE 8080

# -----------------------------------------------------------------------------
# 7) CMD: lệnh chạy khi container khởi động.
#    catalina.sh run = bật Tomcat ở chế độ foreground (chạy nền trước),
#    đây là cách đúng để container "sống" cùng tiến trình Tomcat.
#    (Dòng này image tomcat đã có sẵn, viết lại cho bạn thấy rõ.)
# -----------------------------------------------------------------------------
CMD ["catalina.sh", "run"]
