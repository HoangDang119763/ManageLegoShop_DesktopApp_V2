package BUS;

import DAL.PositionDAL;
import DTO.PositionDTO;

import java.util.ArrayList;

public class PositionBUS extends BaseBUS<PositionDTO, Integer> {
    private static final PositionBUS INSTANCE = new PositionBUS();

    private PositionBUS() {
    }

    public static PositionBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<PositionDTO> getAll() {
        return PositionDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(PositionDTO obj) {
        return obj.getId();
    }

    @Override
    public PositionDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return PositionDAL.getInstance().getById(id);
    }

}
