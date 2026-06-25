# Kế hoạch Triển khai: Quy trình phê duyệt Thuyên chuyển phòng ban, Thăng chức & Giáng chức

Quy trình phân công, chuyển phòng ban, và thay đổi chức vụ hiện tại đang được lưu (hard code) trực tiếp vào Database ngay khi thao tác. Mục tiêu của sự thay đổi này là: **Mọi thao tác thuyên chuyển phòng ban hoặc thăng/giáng chức đều phải sinh ra một Đơn yêu cầu (Form Request) và chỉ có hiệu lực sau khi Business Admin phê duyệt**.

---

## User Review Required

> [!WARNING]
> Kế hoạch này yêu cầu cập nhật lại cấu trúc bảng cơ sở dữ liệu `Form_Requests`, cụ thể là thêm hai cột mới: `targetDepartmentId` và `targetRoleId`. Quá trình này sẽ can thiệp vào `DBInitializer.java`. Nếu Database đang chứa dữ liệu thật (production) hoặc dữ liệu mẫu không được phép reset, sẽ cần bổ sung thêm script SQL ALTER TABLE để migration.

> [!IMPORTANT]
> Quy tắc Thuyên chuyển (Transfer): Khi chuyển phòng ban, bắt buộc phải thay đổi Role của người đó để map đúng với phòng ban mới (Ví dụ: chuyển sang phòng Kế toán thì Role từ `ITEmployee` thành `FIEmployee`).
> Quy tắc Thăng/Giáng chức: Chỉ thay đổi Role (từ Employee lên Manager hoặc ngược lại). Vị trí (`position`) có thể giữ nguyên hoặc được cấu hình thêm.

---

## Proposed Changes

### Database Layer

#### [MODIFY] `dal/DBInitializer.java`
- Trong hàm `createTableFormRequests(Connection conn)`, bổ sung thêm 2 cột:
  - `targetDepartmentId INT NULL`
  - `targetRoleId INT NULL`
- Trong hàm `insertInitialData(Connection conn)`, bổ sung việc insert 2 loại `Form_Types` mới (nếu chưa có):
  - `TRANSFER` (Thuyên chuyển phòng ban)
  - `PROMOTION_DEMOTION` (Thăng chức / Giáng chức)

---

### Data Access Object & Model (DAO/DTO)

#### [MODIFY] `model/FormRequest.java` và [NEW] `dto/TransferRequestDTO.java`
- Bổ sung các biến `targetDepartmentId` (Integer) và `targetRoleId` (Integer) vào `model/FormRequest.java`.
- Tạo mới class `dto/TransferRequestDTO.java` (có thể kế thừa từ `FormRequestDTO` hoặc chứa dữ liệu tương tự) chuyên dùng để chứa dữ liệu cho đơn Thuyên chuyển/Thăng giáng chức thay vì sửa trực tiếp vào `FormRequestDTO` có sẵn.

#### [MODIFY] `dao/FormRequestDAO.java`
- Cập nhật hàm `insertFormRequest(...)` / `addFormRequest(...)` để thêm 2 biến `targetDepartmentId` và `targetRoleId` vào câu lệnh SQL `INSERT`.
- Cập nhật hàm `mapFormRequest(...)` (hoặc các hàm `SELECT`) để lấy ra giá trị `targetDepartmentId` và `targetRoleId` từ `ResultSet` đổ vào Model/DTO.

#### [MODIFY] `dao/EmployeeDAO.java` và `dao/UserDAO.java`
- Đảm bảo đã có hàm xử lý cập nhật phòng ban cho nhân viên (VD: `updateDepartment(employeeId, deptId)`) ở `EmployeeDAO`.
- Đảm bảo đã có hàm xử lý cập nhật role cho user (VD: `updateRole(userId, roleId)`) ở `UserDAO`.

---

### Controller Layer (Xử lý nghiệp vụ)

#### [MODIFY] `controller/ManagerController.java` (hoặc Controller tương ứng xử lý phòng ban)
- Tại hàm `handleAssignDepartment` hoặc các hàm thay đổi phòng ban:
  - Loại bỏ logic trực tiếp lưu vào database (`employeeDAO.assignDepartment(...)`).
  - Thay thế bằng logic: Tạo mới object `FormRequest` với loại đơn là `TRANSFER`.
  - Lưu trữ `targetDepartmentId` dựa theo form submit của user.
  - Tự động map `targetRoleId` tương ứng với phòng ban mới (ví dụ sang IT thì lấy role ITEmployee).
  - Lưu FormRequest vào database thông qua `formRequestDAO.insertFormRequest(...)`.
  - Hiển thị thông báo: *"Yêu cầu thuyên chuyển đã được gửi và đang chờ duyệt."*

#### [NEW] (Thêm chức năng thăng/giáng chức)
- Thêm action `PROMOTION_DEMOTION` trong Controller (Ví dụ: `handlePromotionDemotion`).
- Tạo và lưu `FormRequest` loại `PROMOTION_DEMOTION` ghi lại `targetRoleId` mới.

#### [MODIFY] `controller/FormController.java` (hoặc Controller xử lý Phê duyệt đơn)
- Tại hàm phê duyệt đơn (Approve):
  - Kiểm tra `formType`.
  - **Nếu là `TRANSFER`:**
    - Cập nhật phòng ban mới cho nhân viên.
    - Cập nhật vai trò mới (Role) tương ứng cho User.
  - **Nếu là `PROMOTION_DEMOTION`:**
    - Cập nhật vai trò (Role) mới cho User.
  - Sau khi các xử lý database thành công, cập nhật trạng thái form thành Đã Duyệt (`status = 1` hoặc `APPROVED`).

---

### Giao diện (Views)

#### [MODIFY] `web/.../form_request.jsp` (Giao diện Danh sách Đơn / Request Tab)
- Tích hợp Đơn Thuyên chuyển và Đơn Thăng/Giáng chức vào chung **Tab Gửi Đơn (Request Tab)** giống như các form hiện tại (Nghỉ phép, Tăng ca, Khiếu nại).
- Khi người dùng ấn vào từng loại đơn, giao diện (modal/vùng điền form) sẽ hiển thị ra các trường tương ứng:
  - Nếu là `TRANSFER`: Hiện combobox chọn Phòng ban mới (và Tự động gợi ý Role).
  - Nếu là `PROMOTION_DEMOTION`: Hiện combobox chọn Vai trò (Role) mới.

#### [MODIFY] `web/public/manager/department/assign_department.jsp` (Nếu vẫn giữ luồng cũ)
- Tại trang phân công phòng ban hiện tại, thay vì nút "Lưu" (Lưu ngay) sẽ đổi thành "Gửi yêu cầu thuyên chuyển" (Submit Request) hoặc redirect về trang Request Tab để làm cho đồng bộ.

#### [MODIFY] Giao diện duyệt đơn (Business Admin)
- Ở chi tiết đơn duyệt (View Form Detail), hiển thị thông tin về `targetDepartmentId` (Tên phòng ban mới chuyển đến) và `targetRoleId` (Vai trò mới). Trang duyệt đơn cũng sẽ chung màn hình với các form khác (bấm vào loại nào hiện thông tin loại đó).

---

### Ràng buộc Chấm công & Khiếu nại (Attendance & Complaint Constraints)

#### [MODIFY] `controller/FormController.java` (hoặc Controller xử lý Khiếu nại và Sửa chấm công)
- **Ràng buộc Thời hạn nộp & duyệt đơn**: Đơn khiếu nại chấm công của tháng `X` (và việc duyệt để thay đổi chấm công) chỉ được phép thực hiện từ ngày mùng 1 đến **hết ngày mùng 5 của tháng `X+1`**. Sau ngày 5, hệ thống sẽ khóa không cho phép thay đổi dữ liệu chấm công của tháng `X` nữa.
- **Ràng buộc Ngày khiếu nại (Target Date)**: Nhân viên có thể tạo đơn yêu cầu sửa chấm công cho **bất cứ ngày làm việc nào** của tháng vừa được import (tháng `X`). Cần kiểm tra để đảm bảo ngày xin sửa chấm công (`workDate`) phải nằm trong tháng `X` và là một ngày làm việc hợp lệ.
- **Quy trình Duyệt 2 Bước (Two-step Approval) cho Đơn Khiếu nại**:
  - Khi nhân viên nộp đơn `COMPLAINT`, trạng thái đơn là `0` (Chờ Manager duyệt).
  - **Bước 1 (Manager duyệt)**: Manager của phòng ban duyệt đơn, trạng thái đơn chuyển sang `1` (Manager Approved / Chờ HR duyệt). Lúc này **CHƯA** cập nhật dữ liệu chấm công.
  - **Bước 2 (HR duyệt)**: Nhân viên HR duyệt đơn (những đơn đang ở trạng thái `1`), trạng thái đơn chuyển sang `2` (Fully Approved). **Hệ thống chỉ thực hiện sửa dữ liệu vào bảng chấm công ở bước 2 này.**
    - *Lưu ý quan trọng:* Hệ thống phải kiểm tra xem bản ghi chấm công (của nhân viên đó vào ngày `workDate`) đã tồn tại hay chưa. **Tuyệt đối không được thêm (INSERT) bản ghi chấm công mới** nếu dữ liệu ngày đó không tồn tại trong hệ thống. Chỉ cho phép cập nhật (UPDATE) lên bản ghi đã có. Nếu không tồn tại, trả về lỗi cho HR hoặc không cập nhật gì thêm.
  - Nếu ở bất kỳ bước nào đơn bị Từ chối (Reject), trạng thái chuyển sang `-1` (Rejected) và quy trình kết thúc.

---

## Verification Plan

### Manual Verification
1. Đăng nhập tài khoản có quyền (HR/Manager), vào mục Phân công phòng ban, chọn nhân viên `A` (phòng IT), chọn phân công sang phòng `Finance`. Nhấn "Gửi yêu cầu".
2. Vào Database kiểm tra: 
   - Bản ghi `Employee` chưa bị đổi phòng ban.
   - Bảng `Form_Requests` đã thêm bản ghi mới có type là `TRANSFER`, kèm `targetDepartmentId` (mã phòng FI) và `targetRoleId`.
3. Đăng nhập tài khoản Business Admin (có quyền duyệt form).
4. Vào danh sách Form Request, duyệt đơn chuyển phòng ban của nhân viên `A`.
5. Vào Database kiểm tra lại:
   - Bản ghi `Employee` đã được gán `departmentId` của phòng Finance.
   - Bản ghi `User` (của nhân viên `A`) đã đổi `roleId` thành FIEmployee.
   - Form đã cập nhật trạng thái (Approved).
6. Tương tự với kiểm tra gửi đơn đổi chức vụ (Promotion/Demotion) thay vì chuyển phòng ban, đảm bảo Role đổi thành Manager.
