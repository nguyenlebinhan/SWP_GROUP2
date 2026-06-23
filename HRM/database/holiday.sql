-- Bảng cấu hình ngày lễ động cho module chấm công.
-- Một dòng = một khoảng lễ (startDate..endDate). Khoảng nhiều ngày chỉ cần 1 dòng.

CREATE TABLE IF NOT EXISTS Holiday (
    holidayId   INT AUTO_INCREMENT PRIMARY KEY,
    holidayName NVARCHAR(255) NOT NULL,
    startDate   DATE NOT NULL,
    endDate     DATE NOT NULL,
    isActive    TINYINT(1) NOT NULL DEFAULT 1,
    INDEX idx_holiday_range (startDate, endDate)
);

-- Dữ liệu mẫu
INSERT INTO Holiday (holidayName, startDate, endDate, isActive) VALUES
    (N'Quốc Khánh', '2026-09-02', '2026-09-02', 1),
    (N'Tết Nguyên Đán', '2027-02-15', '2027-02-20', 1);
