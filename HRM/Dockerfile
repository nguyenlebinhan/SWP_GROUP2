
FROM tomcat:10.1-jdk17

# -----------------------------------------------------------------------------
# 2) LABEL: thông tin mô tả (không bắt buộc, chỉ để ghi chú metadata).
# -----------------------------------------------------------------------------
LABEL maintainer="HRM"
LABEL version="1.0"
LABEL description="Hệ thống quản lý nhân sự (HRM) - Java + Tomcat + MySQL"


RUN rm -rf /usr/local/tomcat/webapps/*

COPY dist/HRM.war /usr/local/tomcat/webapps/ROOT.war


ENV DB-URL="jdbc:mysql://mysql:3306/hrm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
ENV DB-USERNAME="root"
ENV DB-PASSWORD="root"


EXPOSE 8080
CMD ["catalina.sh", "run"]