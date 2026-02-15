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

    public ArrayList<EmploymentHistoryDTO> getByEmployeeIdLocalIncrease(int employeeId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        if (employeeId <= 0)
            return result;
        return result;
    }

    public ArrayList<EmploymentHistoryDTO> getByEmployeeIdLocalDecrease(int employeeId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        if (employeeId <= 0)
            return result;
        return result;
    }

    public ArrayList<EmploymentHistoryDTO> getByStatusLocal(int statusId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        return result;
    }

    @Override
    public EmploymentHistoryDTO getById(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }
}
