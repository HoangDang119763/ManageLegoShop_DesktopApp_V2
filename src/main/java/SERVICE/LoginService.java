package SERVICE;

import BUS.AccountBUS;
import BUS.EmployeeBUS;
import BUS.StatusBUS;
import DTO.AccountDTO;
import DTO.EmployeeDTO;
import ENUM.*;
import ENUM.ServiceAccessCode;
import UTILS.AvailableUtils;

public class LoginService {
    private static final LoginService INSTANCE = new LoginService();

    public static LoginService getInstance() {
        return INSTANCE;
    }

    public int checkLogin(AccountDTO account) {
        EmployeeBUS empBus = EmployeeBUS.getInstance();
        AccountBUS accBus = AccountBUS.getInstance();
        StatusBUS statusBus = StatusBUS.getInstance();
        if (statusBus.isLocalEmpty()) {
            statusBus.loadLocal();

        }
        // Load Account if the local list is empty
        if (accBus.isLocalEmpty()) {
            accBus.loadLocal();
        }

        // Check again if the list is still empty after loading
        if (accBus.isLocalEmpty()) {
            return -1; // Accounts not available
        }

        // Check the login
        int currAcc = accBus.checkLogin(account.getUsername(), account.getPassword(),
                ServiceAccessCode.LOGIN_SERVICE.getCode());

        // If login failed
        if (currAcc < 0) {
            return currAcc;
        }

        // Load Employee if the local list is empty
        if (empBus.isLocalEmpty()) {
            empBus.loadLocal();
        }

        // Check again if the list is still empty after loading
        if (empBus.isLocalEmpty()) {
            return -1; // Employees not available
        }

        // Get the Employee DTO by account ID
        EmployeeDTO employee = empBus.getByAccountIdLocal(currAcc);

        // Store the logged-in employee in session
        SessionManagerService.getInstance()
                .setLoggedInEmployee(empBus.getByIdLocal(employee.getId()));

        // Check employee status and role
        boolean isActive = employee.getStatusId() == AvailableUtils.getInstance().getStatusIdByTypeAndName(
                StatusType.EMPLOYEE, Status.Employee.ACTIVE);
        System.out.println(AvailableUtils.getInstance().getStatusIdByTypeAndName(
                StatusType.EMPLOYEE, Status.Employee.ACTIVE));
        boolean hasValidRole = employee.getRoleId() != 0;
        boolean hasAccess = SessionManagerService.getInstance().numAllowedModules() != 0;
        if (!isActive || !hasValidRole || !hasAccess) {
            return -3; // Employee inactive or invalid role or no access
        }
        return currAcc;
    }
}
