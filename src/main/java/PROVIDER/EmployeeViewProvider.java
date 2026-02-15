package PROVIDER;

import BUS.AccountBUS;
import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.RoleBUS;
import BUS.SalaryBUS;
import BUS.StatusBUS;
import BUS.TaxBUS;
import DTO.AccountDTO;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.EmployeeDetailDTO;
import DTO.RoleDTO;
import DTO.SalaryDTO;
import DTO.StatusDTO;
import DTO.TaxDTO;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provider để transform dữ liệu Employee sang EmployeeDetailDTO
 * Single Responsibility: Chỉ xử lý transformation từ EmployeeDTO + BUS data
 * sang EmployeeDetailDTO
 * Filter logic được tách ra EmployeeDetailFilterer
 */
public class EmployeeViewProvider {

    private static final EmployeeViewProvider INSTANCE = new EmployeeViewProvider();

    private EmployeeViewProvider() {
    }

    public static EmployeeViewProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Load tất cả data từ BUS (sử dụng mapLocal đã có sẵn trong BaseBUS)
     */
    private void loadAllData() {
        // Data loads on-demand from DB via BUS.getById() calls
        // No need to pre-load All anymore with stateless architecture
    }

    /**
     * Convert list EmployeeDTO -> EmployeeDetailDTO
     */
    public ArrayList<EmployeeDetailDTO> toTableDTOs(List<EmployeeDTO> employees) {
        loadAllData();

        return employees.stream()
                .map(this::transform)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get single employee detail
     */
    public EmployeeDetailDTO getDetailById(int employeeId) {
        EmployeeDTO emp = EmployeeBUS.getInstance().getById(employeeId);
        if (emp == null)
            return null;

        loadAllData();
        return transform(emp);
    }

    /**
     * Transform core method - no need to pre-load, will call DB on-demand for each
     * getById()
     */
    private EmployeeDetailDTO transform(EmployeeDTO emp) {
        RoleBUS roleBUS = RoleBUS.getInstance();
        SalaryBUS salaryBUS = SalaryBUS.getInstance();
        StatusBUS statusBUS = StatusBUS.getInstance();
        AccountBUS accountBUS = AccountBUS.getInstance();
        DepartmentBUS departmentBUS = DepartmentBUS.getInstance();
        TaxBUS taxBUS = TaxBUS.getInstance();

        RoleDTO role = roleBUS.getById(emp.getRoleId());
        SalaryDTO salary = role != null ? salaryBUS.getById(role.getSalaryId()) : null;
        StatusDTO empStatus = statusBUS.getById(emp.getStatusId());
        AccountDTO account = accountBUS.getById(emp.getAccountId());
        DepartmentDTO department = emp.getDepartmentId() != null ? departmentBUS.getById(emp.getDepartmentId())
                : null;
        TaxDTO tax = taxBUS.getById(emp.getId());

        StatusDTO accStatus = account != null ? statusBUS.getById(account.getStatusId()) : null;

        return EmployeeDetailDTO.builder()
                // BaseInformation fields
                .id(emp.getId())
                .dateOfBirth(emp.getDateOfBirth())
                .createdAt(emp.getCreatedAt())
                .updatedAt(emp.getUpdatedAt())
                .statusId(emp.getStatusId())

                // Employee display ID
                .employeeId(emp.getId())

                // Core employee info
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .gender(emp.getGender())

                // Department info
                .departmentId(emp.getDepartmentId())
                .departmentName(department != null ? department.getName() : "")

                // Role info
                .roleId(emp.getRoleId())
                .roleName(role != null ? role.getName() : "")

                // Account info
                .accountId(emp.getAccountId())
                .username(account != null ? account.getUsername() : "")
                .accountStatusId(account != null ? account.getStatusId() : 0)
                .accountStatus(accStatus != null ? accStatus.getDescription() : "")

                // Employee status
                .statusDescription(empStatus != null ? empStatus.getDescription() : "")

                // Salary info
                .salaryId(role != null ? role.getSalaryId() : 0)
                .baseSalary(salary != null ? salary.getBase() : null)
                .salaryCoefficient(salary != null ? salary.getCoefficient() : null)

                // Tax info
                .taxId(tax != null ? tax.getId() : 0)
                .numDependents(tax != null ? tax.getNumDependents() : null)

                // Health & Support flags
                .healthInsCode(emp.getHealthInsCode() != null ? emp.getHealthInsCode() : "")
                .isSocialInsurance(emp.isSocialInsurance())
                .isUnemploymentInsurance(emp.isUnemploymentInsurance())
                .isPersonalIncomeTax(emp.isPersonalIncomeTax())
                .isTransportationSupport(emp.isTransportationSupport())
                .isAccommodationSupport(emp.isAccommodationSupport())

                .build();
    }
}
