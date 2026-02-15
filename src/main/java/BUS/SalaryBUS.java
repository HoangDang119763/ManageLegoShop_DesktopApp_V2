package BUS;

import DAL.SalaryDAL;
import DTO.SalaryDTO;

import java.util.ArrayList;

public class SalaryBUS extends BaseBUS<SalaryDTO, Integer> {
    private static final SalaryBUS INSTANCE = new SalaryBUS();

    private SalaryBUS() {
    }

    public static SalaryBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<SalaryDTO> getAll() {
        return SalaryDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(SalaryDTO obj) {
        return obj.getId();
    }

    @Override
    public SalaryDTO getById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return SalaryDAL.getInstance().getById(id);
    }

}
