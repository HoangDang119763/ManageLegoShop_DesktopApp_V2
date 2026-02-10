package BUS;

import DAL.EmploymentHistoryDAL;
import DTO.EmploymentHistoryDTO;
import java.util.ArrayList;

public class EmploymentHistoryBUS extends BaseBUS<EmploymentHistoryDTO, Integer> {
    private static final EmploymentHistoryBUS INSTANCE = new EmploymentHistoryBUS();

    private EmploymentHistoryBUS() {
    }

    public static EmploymentHistoryBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<EmploymentHistoryDTO> getAll() {
        return EmploymentHistoryDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(EmploymentHistoryDTO obj) {
        return obj.getId();
    }

    public ArrayList<EmploymentHistoryDTO> getAllLocal() {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        for (EmploymentHistoryDTO history : arrLocal) {
            result.add(new EmploymentHistoryDTO(history));
        }
        return result;
    }

    public ArrayList<EmploymentHistoryDTO> getByEmployeeIdLocalIncrease(int employeeId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        if (employeeId <= 0)
            return result;
        for (EmploymentHistoryDTO history : arrLocal) {
            if (history.getEmployeeId() == employeeId) {
                result.add(new EmploymentHistoryDTO(history));
            }
        }
        return result;
    }

    public ArrayList<EmploymentHistoryDTO> getByEmployeeIdLocalDecrease(int employeeId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        if (employeeId <= 0)
            return result;
        for (int i = arrLocal.size() - 1; i >= 0; i--) {
            EmploymentHistoryDTO history = arrLocal.get(i);
            if (history.getEmployeeId() == employeeId) {
                result.add(new EmploymentHistoryDTO(history));
            }
        }
        return result;
    }

    public ArrayList<EmploymentHistoryDTO> getByStatusLocal(int statusId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        for (EmploymentHistoryDTO history : arrLocal) {
            if (history.getStatusId() == statusId) {
                result.add(new EmploymentHistoryDTO(history));
            }
        }
        return result;
    }
}
