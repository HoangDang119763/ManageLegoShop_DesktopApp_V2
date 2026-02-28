package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO chứa thông tin cơ bản lịch sử công tác của nhân viên
 * Gồm: departmentId, departmentName, positionId, positionName, effectiveDate,
 * createdAt
 * Dùng cho display trong TableView
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmploymentHistoryDetailBasicDTO {
    private int departmentId; // ID phòng ban
    private String departmentName; // Tên phòng ban
    private int positionId; // ID vị trí
    private String positionName; // Tên vị trí
    private LocalDate effectiveDate; // Ngày áp dụng
    private int statusId;
    private String statusDescription;

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
