package BUS;

import DAL.DepartmentDAL;
import DTO.DepartmentDTO;
import DTO.StatusDTO;
import ENUM.StatusType;
import ENUM.Status;

import java.util.ArrayList;

public class DepartmentBUS extends BaseBUS<DepartmentDTO, Integer> {
    private static final DepartmentBUS INSTANCE = new DepartmentBUS();

    private DepartmentBUS() {
    }

    public static DepartmentBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<DepartmentDTO> getAll() {
        return DepartmentDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(DepartmentDTO obj) {
        return obj.getId();
    }

    @Override
    public DepartmentDTO getById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return DepartmentDAL.getInstance().getById(id);
    }

    public boolean isDepartmentActive(int departmentId) {
        StatusDTO activeStatus = StatusBUS.getInstance().getByTypeAndStatusName(StatusType.DEPARTMENT,
                Status.Department.ACTIVE);
        if (activeStatus == null) {
            return false; // Không tìm thấy trạng thái ACTIVE cho DEPARTMENT
        }
        return DepartmentDAL.getInstance().existsByIdAndStatus(departmentId, activeStatus.getId());
    }
}
