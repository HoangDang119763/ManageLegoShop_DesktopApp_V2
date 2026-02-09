package BUS;

import DAL.SalaryDAL;
import DTO.SalaryDTO;

import java.util.ArrayList;
import java.util.Objects;

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

}
