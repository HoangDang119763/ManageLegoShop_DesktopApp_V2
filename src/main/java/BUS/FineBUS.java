package BUS;

import DTO.FineDTO;
import DAL.FineDAL;
import UTILS.ValidationUtils;
import SERVICE.AuthorizationService;
import java.math.BigDecimal;
import java.util.ArrayList;

public class FineBUS extends BaseBUS<FineDTO, Integer> {
    public static final FineBUS INSTANCE = new FineBUS();

    private FineBUS() {
    }

    public static FineBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<FineDTO> getAll() {
        return FineDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(FineDTO obj) {
        return obj.getId();
    }

    public FineDTO getById(Integer id) {
        return FineDAL.getInstance().getById(id);
    }

    public ArrayList<FineDTO> getByFineLevel(String fineLevel) {
        ArrayList<FineDTO> allFines = getAll();
        ArrayList<FineDTO> result = new ArrayList<>();
        for (FineDTO fine : allFines) {
            if (fine.getFineLevel() != null && fine.getFineLevel().equals(fineLevel)) {
                result.add(fine);
            }
        }
        return result;
    }

    public ArrayList<FineDTO> getByEmployeeId(int employeeId) {
        ArrayList<FineDTO> allFines = getAll();
        ArrayList<FineDTO> result = new ArrayList<>();
        for (FineDTO fine : allFines) {
            if (fine.getEmployeeId() == employeeId) {
                result.add(fine);
            }
        }
        return result;
    }

    public boolean insert(FineDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidFineInput(obj)) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (FineDAL.getInstance().insert(obj)) {
            if (arrLocal.isEmpty()) {
                loadLocal();
            } else {
                arrLocal.add(new FineDTO(obj));
            }
            return true;
        }
        return false;
    }

    public boolean update(FineDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidFineInput(obj)) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (FineDAL.getInstance().update(obj)) {
            for (FineDTO fine : arrLocal) {
                if (fine.getId() == obj.getId()) {
                    fine.setReason(obj.getReason());
                    fine.setCreatedAt(obj.getCreatedAt());
                    fine.setFineLevel(obj.getFineLevel());
                    fine.setAmount(obj.getAmount());
                    fine.setFinePay(obj.getFinePay());
                    fine.setEmployeeId(obj.getEmployeeId());
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
        if (FineDAL.getInstance().delete(id)) {
            arrLocal.removeIf(fine -> fine.getId() == id);
            return true;
        }
        return false;
    }

    private boolean isValidFineInput(FineDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getReason() == null || obj.getReason().trim().isEmpty()) {
            return false;
        }
        ValidationUtils validator = ValidationUtils.getInstance();
        if (!validator.validateVietnameseText255(obj.getReason())) {
            return false;
        }
        if (obj.getFineLevel() == null || obj.getFineLevel().trim().isEmpty()) {
            return false;
        }
        if (obj.getAmount() == null || !validator.validateBigDecimal(obj.getAmount(), 15, 2, false)) {
            return false;
        }
        if (obj.getFinePay() == null) {
            obj.setFinePay(BigDecimal.ZERO);
        } else if (!validator.validateBigDecimal(obj.getFinePay(), 15, 2, true)) {
            return false;
        }
        if (obj.getEmployeeId() <= 0) {
            return false;
        }
        return true;
    }

}
