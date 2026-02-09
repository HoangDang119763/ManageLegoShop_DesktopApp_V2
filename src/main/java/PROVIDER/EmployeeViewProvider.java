package PROVIDER;

import BUS.AccountBUS;
import BUS.EmployeeBUS;
import BUS.RoleBUS;
import BUS.SalaryBUS;
import BUS.StatusBUS;
import BUS.TaxBUS;
import DTO.AccountDTO;
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

    // Cache maps per call (not global cache)
    private Map<Integer, RoleDTO> roleMap;
    private Map<Integer, SalaryDTO> salaryMap;
    private Map<Integer, StatusDTO> statusMap;
    private Map<Integer, AccountDTO> accountMap;
    private Map<Integer, TaxDTO> taxMap;

    /**
     * Build maps once per request
     */
    private void buildMaps() {
        RoleBUS roleBUS = RoleBUS.getInstance();
        SalaryBUS salaryBUS = SalaryBUS.getInstance();
        StatusBUS statusBUS = StatusBUS.getInstance();
        AccountBUS accountBUS = AccountBUS.getInstance();
        TaxBUS taxBUS = TaxBUS.getInstance();
        if (roleBUS.isLocalEmpty())
            roleBUS.loadLocal();
        if (salaryBUS.isLocalEmpty())
            salaryBUS.loadLocal();
        if (statusBUS.isLocalEmpty())
            statusBUS.loadLocal();
        if (accountBUS.isLocalEmpty())
            accountBUS.loadLocal();
        if (taxBUS.isLocalEmpty())
            taxBUS.loadLocal();
        roleMap = roleBUS.getAllLocal().stream()
                .collect(Collectors.toMap(RoleDTO::getId, r -> r));

        salaryMap = salaryBUS.getAllLocal().stream()
                .collect(Collectors.toMap(SalaryDTO::getId, s -> s));

        statusMap = statusBUS.getAllLocal().stream()
                .collect(Collectors.toMap(StatusDTO::getId, s -> s));

        accountMap = accountBUS.getAllLocal().stream()
                .collect(Collectors.toMap(AccountDTO::getId, a -> a));

        taxMap = taxBUS.getAllLocal().stream()
                .collect(Collectors.toMap(TaxDTO::getEmployeeId, t -> t));
    }

    /**
     * Convert list EmployeeDTO -> EmployeeDetailDTO
     */
    public ArrayList<EmployeeDetailDTO> toTableDTOs(List<EmployeeDTO> employees) {
        buildMaps();

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

        buildMaps();
        return transform(emp);
    }

    /**
     * Transform core method
     */
    private EmployeeDetailDTO transform(EmployeeDTO emp) {

        RoleDTO role = roleMap.get(emp.getRoleId());
        SalaryDTO salary = role != null ? salaryMap.get(role.getSalaryId()) : null;
        StatusDTO empStatus = statusMap.get(emp.getStatusId());
        AccountDTO account = accountMap.get(emp.getAccountId());
        TaxDTO tax = taxMap.get(emp.getId());

        StatusDTO accStatus = account != null ? statusMap.get(account.getStatusId()) : null;

        return EmployeeDetailDTO.builder()
                .employeeId(emp.getId())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .gender(emp.getGender())

                .roleId(emp.getRoleId())
                .roleName(role != null ? role.getName() : "")

                .salaryId(role != null ? role.getSalaryId() : 0)
                .baseSalary(salary != null ? salary.getBase() : null)
                .salaryCoefficient(salary != null ? salary.getCoefficient() : null)

                .accountId(emp.getAccountId())
                .username(account != null ? account.getUsername() : "")
                .accountStatusId(account != null ? account.getStatusId() : 0)
                .accountStatus(accStatus != null ? accStatus.getDescription() : "")

                .statusId(emp.getStatusId())
                .statusDescription(empStatus != null ? empStatus.getDescription() : "")

                .taxId(tax != null ? tax.getId() : 0)
                .numDependents(tax != null ? tax.getNumDependents() : 0)

                .build();
    }
}
