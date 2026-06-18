package dto;

/**
 * DTO dành riêng cho Đơn Khiếu Nại (formTypeCode = "COMPLAINT").
 * Hiện tại không có trường bổ sung ngoài các trường chung trong FormRequestDTO.
 * Class này tồn tại để dễ mở rộng sau này và để phân biệt rõ loại đơn khi cast.
 */
public class ComplaintFormRequestDTO extends FormRequestDTO {
    // Có thể bổ sung trường riêng sau (vd: complaintCategory, targetEmployeeId...)
}
