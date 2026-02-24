package BUS;

import DTO.LeaveTypeDTO;
import DAL.LeaveTypeDAL;
import java.util.ArrayList;

public class LeaveTypeBUS extends BaseBUS<LeaveTypeDTO, Integer> {
    public static final LeaveTypeBUS INSTANCE = new LeaveTypeBUS();

    private LeaveTypeBUS() {
    }

    public static LeaveTypeBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<LeaveTypeDTO> getAll() {
        return LeaveTypeDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(LeaveTypeDTO obj) {
        return obj.getId();
    }

    public LeaveTypeDTO getById(Integer id) {
        return LeaveTypeDAL.getInstance().getById(id);
    }

    public boolean insert(LeaveTypeDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidLeaveTypeInput(obj)) {
            return false;
        }
        return LeaveTypeDAL.getInstance().insert(obj);
    }

    public boolean update(LeaveTypeDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidLeaveTypeInput(obj)) {
            return false;
        }
        return LeaveTypeDAL.getInstance().update(obj);
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }
        return LeaveTypeDAL.getInstance().delete(id);
    }

    private boolean isValidLeaveTypeInput(LeaveTypeDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getName() == null || obj.getName().isEmpty()) {
            return false;
        }
        return true;
    }
}
