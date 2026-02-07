package BUS;

import DAL.EmploymentHistoryDAL;
import DTO.EmploymentHistoryDTO;
import java.util.ArrayList;
import java.util.Objects;

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

    public EmploymentHistoryDTO getByIdLocal(int id) {
        if (id <= 0)
            return null;
        for (EmploymentHistoryDTO history : arrLocal) {
            if (Objects.equals(history.getId(), id)) {
                return new EmploymentHistoryDTO(history);
            }
        }
        return null;
    }

    public ArrayList<EmploymentHistoryDTO> getAllLocal() {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        for (EmploymentHistoryDTO history : arrLocal) {
            result.add(new EmploymentHistoryDTO(history));
        }
        return result;
    }

    public ArrayList<EmploymentHistoryDTO> getByEmployeeIdLocal(int employeeId) {
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
