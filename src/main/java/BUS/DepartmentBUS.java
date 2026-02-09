package BUS;

import DAL.DepartmentDAL;
import DTO.DepartmentDTO;
import java.util.ArrayList;
import java.util.Objects;

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

    public ArrayList<DepartmentDTO> getAllLocal() {
        ArrayList<DepartmentDTO> result = new ArrayList<>();
        for (DepartmentDTO dept : arrLocal) {
            result.add(new DepartmentDTO(dept));
        }
        return result;
    }

    public DepartmentDTO getByNameLocal(String name) {
        if (name == null || name.isEmpty())
            return null;
        for (DepartmentDTO dept : arrLocal) {
            if (dept.getName() != null && dept.getName().equalsIgnoreCase(name)) {
                return new DepartmentDTO(dept);
            }
        }
        return null;
    }
}
