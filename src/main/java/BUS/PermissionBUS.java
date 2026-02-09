package BUS;

import DAL.PermissionDAL;
import DTO.PermissionDTO;
import java.util.ArrayList;
import java.util.Objects;

public class PermissionBUS extends BaseBUS<PermissionDTO, Integer> {
    private static final PermissionBUS INSTANCE = new PermissionBUS();

    private PermissionBUS() {
    }

    public static PermissionBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<PermissionDTO> getAll() {
        return PermissionDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(PermissionDTO obj) {
        return obj.getId();
    }

    public PermissionDTO getByPermissionKeyLocal(String permissionKey) {
        if (permissionKey == null || permissionKey.isEmpty())
            return null;
        for (PermissionDTO permission : arrLocal) {
            if (permissionKey.equalsIgnoreCase(permission.getPermissionKey())) {
                return new PermissionDTO(permission);
            }
        }
        return null;
    }
}
