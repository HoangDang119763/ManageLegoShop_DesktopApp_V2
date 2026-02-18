package DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Bundle của Tab 1: Hồ sơ nhân viên
 * Chứa 3 DTO: PersonalInfo + JobInfo + PayrollInfo
 * Giảm số lần gọi BUS từ 3 xuống 1
 */
@Data
@Builder
@AllArgsConstructor
public class EmployeePersonalInfoBundle {
    private EmployeePersonalInfoDTO personalInfo;
    private EmployeeJobInfoDTO jobInfo;
    private EmployeePayrollInfoDTO payrollInfo;
}
