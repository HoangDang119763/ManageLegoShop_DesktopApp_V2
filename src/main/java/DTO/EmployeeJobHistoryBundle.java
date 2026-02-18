package DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Bundle của Tab 2: Lương & Công tác
 * Chứa 2 DTO: JobInfo + PayrollInfo
 * Giảm số lần gọi BUS từ 2 xuống 1
 */
@Data
@Builder
@AllArgsConstructor
public class EmployeeJobHistoryBundle {
    private EmployeeJobInfoDTO jobInfo;
    private EmployeePayrollInfoDTO payrollInfo;
}
