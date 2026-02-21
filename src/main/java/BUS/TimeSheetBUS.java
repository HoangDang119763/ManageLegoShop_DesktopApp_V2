package BUS;

import DTO.TimeSheetDTO;
import DAL.ImportDAL;
import DAL.TimeSheetDAL;
import java.util.ArrayList;
import java.util.HashMap;

public class TimeSheetBUS extends BaseBUS<TimeSheetDTO, Integer> {
    public static final TimeSheetBUS INSTANCE = new TimeSheetBUS();
    private final HashMap<Integer, ArrayList<TimeSheetDTO>> mapByEmployeeId = new HashMap<>();

    private TimeSheetBUS() {
    }

    public static TimeSheetBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<TimeSheetDTO> getAll() {
        return TimeSheetDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(TimeSheetDTO obj) {
        return obj.getId();
    }

    public TimeSheetDTO getById(Integer id) {
        return TimeSheetDAL.getInstance().getById(id);
    }

    public ArrayList<TimeSheetDTO> getByEmployeeId(int employeeId) {
        if (employeeId <= 0)
            return new ArrayList<>();
        ArrayList<TimeSheetDTO> result = mapByEmployeeId.get(employeeId);
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }

    public boolean insert(TimeSheetDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidTimeSheetInput(obj)) {
            return false;
        }

        return false;
    }

    public boolean update(TimeSheetDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidTimeSheetInput(obj)) {
            return false;
        }

        return false;
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }

        return false;
    }

    private boolean isValidTimeSheetInput(TimeSheetDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getEmployeeId() <= 0) {
            return false;
        }
        if (obj.getCheckIn() == null) {
            return false;
        }
        return true;
    }

    public boolean isEmployeeInAnyTimeSheet(int employeeId) {
        if (employeeId <= 0)
            return false;
        return TimeSheetDAL.getInstance().existsByEmployeeId(employeeId);
    }

}
