package DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO để quản lý vị trí/chức vụ của nhân viên
 * Bảng position chứa thông tin vị trí, lương cơ bản
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionDTO {
    private Integer id;
    private String name; // Tên vị trí
    private BigDecimal wage; // Lương cơ bản
    private Integer minExperience; // Số năm kinh nghiệm tối thiểu
    private Integer maxExperience; // Số năm kinh nghiệm tối đa
    private LocalDateTime createdAt; // Ngày tạo
    private LocalDateTime updatedAt; // Ngày cập nhật

    public PositionDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
