package BUS;

import DTO.HolidayDTO;
import DAL.HolidayDAL;
import UTILS.ValidationUtils;
import SERVICE.AuthorizationService;
import java.time.LocalDate;
import java.util.ArrayList;

public class HolidayBUS extends BaseBUS<HolidayDTO, Integer> {
    public static final HolidayBUS INSTANCE = new HolidayBUS();

    private HolidayBUS() {
    }

    public static HolidayBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<HolidayDTO> getAll() {
        return HolidayDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(HolidayDTO obj) {
        return obj.getId();
    }

    public HolidayDTO getById(Integer id) {
        return HolidayDAL.getInstance().getById(id);
    }

    public ArrayList<HolidayDTO> getByYear(int year) {
        ArrayList<HolidayDTO> allHolidays = getAll();
        ArrayList<HolidayDTO> result = new ArrayList<>();
        for (HolidayDTO holiday : allHolidays) {
            if (holiday.getDate() != null && holiday.getDate().getYear() == year) {
                result.add(holiday);
            }
        }
        return result;
    }

    public boolean isHoliday(LocalDate date) {
        ArrayList<HolidayDTO> allHolidays = getAll();
        for (HolidayDTO holiday : allHolidays) {
            if (holiday.getDate() != null && holiday.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    public boolean insert(HolidayDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidHolidayInput(obj)) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (HolidayDAL.getInstance().insert(obj)) {
            if (arrLocal.isEmpty()) {
                loadLocal();
            } else {
                arrLocal.add(new HolidayDTO(obj));
            }
            return true;
        }
        return false;
    }

    public boolean update(HolidayDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidHolidayInput(obj)) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (HolidayDAL.getInstance().update(obj)) {
            for (HolidayDTO holiday : arrLocal) {
                if (holiday.getId() == obj.getId()) {
                    holiday.setName(obj.getName());
                    holiday.setDate(obj.getDate());
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
        if (HolidayDAL.getInstance().delete(id)) {
            arrLocal.removeIf(holiday -> holiday.getId() == id);
            return true;
        }
        return false;
    }

    private boolean isValidHolidayInput(HolidayDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getName() == null || obj.getName().trim().isEmpty()) {
            return false;
        }
        ValidationUtils validator = ValidationUtils.getInstance();
        if (!validator.validateStringLength(obj.getName(), 255)) {
            return false;
        }
        if (obj.getDate() == null) {
            return false;
        }
        return true;
    }
}
