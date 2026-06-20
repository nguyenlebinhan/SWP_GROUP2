package model;

/**
 * Model dành riêng cho Đơn Khiếu Nại.
 * Kế thừa FormRequest, hiện tại không có trường bổ sung.
 * Tồn tại để phân biệt rõ loại đơn và dễ mở rộng sau này.
 */
public class ComplaintFormRequest extends FormRequest {
    // Có thể bổ sung trường riêng sau (vd: complaintCategory, targetEmployeeId...)
}
