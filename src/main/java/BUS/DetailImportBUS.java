package BUS;

import DAL.DetailImportDAL;
import DTO.BUSResult;
import DTO.DetailImportDTO;
import ENUM.BUSOperationResult;
import ENUM.ServiceAccessCode;
import UTILS.AppMessages;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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

    public BUSResult getAllDetailImportByImportId(int importId) {
        if (importId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS, new ArrayList<>());
        ArrayList<DetailImportDTO> detailImports = DetailImportDAL.getInstance().getAllDetailImportByImportId(importId);
        if (detailImports.isEmpty())
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND, new ArrayList<>());
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.IMPORT_DETAIL_LOAD_SUCCESS, detailImports);
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

    /**
     * Insert Detail Imports using provided connection (for transaction)
     * Thêm chi tiết phiếu nhập sử dụng connection được cung cấp (cho transaction)
     *
     * @param conn       the database connection
     * @param importId   the import id
     * @param detailList the list of detail import records
     * @return true if success, false otherwise
     */
    public boolean insertDetailImportWithConn(Connection conn, int importId, List<DetailImportDTO> detailList) {
        if (conn == null || importId <= 0 || detailList == null || detailList.isEmpty())
            return false;

        return DetailImportDAL.getInstance().insertAllDetailImportByImportId(conn, importId,
                new ArrayList<>(detailList));
    }
}
