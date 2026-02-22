package BUS;

import DAL.DetailImportDAL;
import DTO.DetailImportDTO;
import ENUM.ServiceAccessCode;

import java.util.ArrayList;

public class DetailImportBUS extends BaseBUS<DetailImportDTO, Integer> {
    private static final DetailImportBUS INSTANCE = new DetailImportBUS();

    public static DetailImportBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<DetailImportDTO> getAll() {
        return DetailImportDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(DetailImportDTO obj) {
        return obj.getImportId();
    }

    public boolean delete(Integer id, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.IMPORT_DETAILIMPORT_SERVICE || id == null || id <= 0)
            return false;
        if (!DetailImportDAL.getInstance().deleteAllDetailImportByImportId(id)) {
            return false;
        }
        return true;
    }

    public ArrayList<DetailImportDTO> getAllDetailImportByImportId(int importId) {
        if (importId <= 0)
            return null;
        return DetailImportDAL.getInstance().getAllDetailImportByImportId(importId);
    }

    public boolean createDetailImportByImportId(int importId, int employee_roleId, ArrayList<DetailImportDTO> list,
            ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.IMPORT_DETAILIMPORT_SERVICE || list == null || list.isEmpty()
                || importId <= 0)
            return false;

        if (!DetailImportDAL.getInstance().insertAllDetailImportByImportId(importId, list)) {
            return false;
        }
        ArrayList<DetailImportDTO> newDetailImport = DetailImportDAL.getInstance()
                .getAllDetailImportByImportId(importId);

        return true;
    }

    public boolean insertRollbackDetailImport(ArrayList<DetailImportDTO> list, int employee_roleId,
            ServiceAccessCode codeAccess,
            int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.IMPORT_DETAILIMPORT_SERVICE || list == null || list.isEmpty())
            return false;

        if (!DetailImportDAL.getInstance().insertAllDetailImportByImportId(list.get(0).getImportId(), list)) {
            return false;
        }
        ArrayList<DetailImportDTO> newDetailImport = DetailImportDAL.getInstance()
                .getAllDetailImportByImportId(list.get(0).getImportId());
        return true;
    }

    @Override
    public DetailImportDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return DetailImportDAL.getInstance().getById(id);
    }
}
