package BUS;

import DTO.TimeSheetDTO;
import DAL.TimeSheetDAL;
import SERVICE.AuthorizationService;
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

    @Override
    public void loadLocal() {
        super.loadLocal();
        mapByEmployeeId.clear();
        for (TimeSheetDTO ts : arrLocal) {
            if (ts.getEmployeeId() > 0) {
                mapByEmployeeId.computeIfAbsent(ts.getEmployeeId(), k -> new ArrayList<>()).add(ts);
            }
        }
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
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (TimeSheetDAL.getInstance().insert(obj)) {
            if (arrLocal.isEmpty()) {
                loadLocal();
            } else {
                arrLocal.add(new TimeSheetDTO(obj));
            }
            return true;
        }
        return false;
    }

    public boolean update(TimeSheetDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidTimeSheetInput(obj)) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (TimeSheetDAL.getInstance().update(obj)) {
            for (TimeSheetDTO ts : arrLocal) {
                if (ts.getId() == obj.getId()) {
                    ts.setEmployeeId(obj.getEmployeeId());
                    ts.setCheckIn(obj.getCheckIn());
                    ts.setCheckOut(obj.getCheckOut());
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (TimeSheetDAL.getInstance().delete(id)) {
            arrLocal.removeIf(ts -> ts.getId() == id);
            return true;
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
}
