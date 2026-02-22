package BUS;

import DAL.DepartmentDAL;
import DTO.DepartmentDTO;
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

}
