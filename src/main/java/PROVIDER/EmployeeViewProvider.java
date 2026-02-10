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
        RoleBUS roleBUS = RoleBUS.getInstance();
        SalaryBUS salaryBUS = SalaryBUS.getInstance();
        StatusBUS statusBUS = StatusBUS.getInstance();
        AccountBUS accountBUS = AccountBUS.getInstance();
        DepartmentBUS departmentBUS = DepartmentBUS.getInstance();
        TaxBUS taxBUS = TaxBUS.getInstance();

        if (roleBUS.isLocalEmpty())
            roleBUS.loadLocal();
        if (salaryBUS.isLocalEmpty())
            salaryBUS.loadLocal();
        if (statusBUS.isLocalEmpty())
            statusBUS.loadLocal();
        if (accountBUS.isLocalEmpty())
            accountBUS.loadLocal();
        if (departmentBUS.isLocalEmpty())
            departmentBUS.loadLocal();
        if (taxBUS.isLocalEmpty())
            taxBUS.loadLocal();
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
        EmployeeDTO emp = EmployeeBUS.getInstance().getByIdLocal(employeeId);
        if (emp == null)
            return null;

        loadAllData();
        return transform(emp);
    }

    /**
     * Transform core method - sử dụng getByIdLocal từ BaseBUS (map lookup O(1))
     */
    private EmployeeDetailDTO transform(EmployeeDTO emp) {
        RoleBUS roleBUS = RoleBUS.getInstance();
        SalaryBUS salaryBUS = SalaryBUS.getInstance();
        StatusBUS statusBUS = StatusBUS.getInstance();
        AccountBUS accountBUS = AccountBUS.getInstance();
        DepartmentBUS departmentBUS = DepartmentBUS.getInstance();
        TaxBUS taxBUS = TaxBUS.getInstance();

        RoleDTO role = roleBUS.getByIdLocal(emp.getRoleId());
        SalaryDTO salary = role != null ? salaryBUS.getByIdLocal(role.getSalaryId()) : null;
        StatusDTO empStatus = statusBUS.getByIdLocal(emp.getStatusId());
        AccountDTO account = accountBUS.getByIdLocal(emp.getAccountId());
        DepartmentDTO department = emp.getDepartmentId() != null ? departmentBUS.getByIdLocal(emp.getDepartmentId())
                : null;
        TaxDTO tax = taxBUS.getByIdLocal(emp.getId());

        StatusDTO accStatus = account != null ? statusBUS.getByIdLocal(account.getStatusId()) : null;

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
