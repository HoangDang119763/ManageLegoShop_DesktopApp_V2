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

    public SalaryDTO getByIdLocal(int id) {
        if (id <= 0)
            return null;
        for (SalaryDTO salary : arrLocal) {
            if (Objects.equals(salary.getId(), id)) {
                return new SalaryDTO(salary);
            }
        }
        return null;
    }
}
