package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDisplayDTO {
    private Integer employeeId;
    private String fullName; // firstName + lastName
    private String gender;
    private Integer roleId;
    private String roleName;
    private BigDecimal salary; // Base salary from role's salary_id
    private BigDecimal efficientSalary; // Salary coefficient
    private String username; // From account table
    private Integer statusId;
    private String statusDescription;
}
