package BUS;

import DAL.DetailInvoiceDAL;
import DTO.BUSResult;
import DTO.DetailInvoiceDTO;
import ENUM.BUSOperationResult;
import ENUM.ServiceAccessCode;
import UTILS.AppMessages;

import java.util.ArrayList;

public class DetailInvoiceBUS extends BaseBUS<DetailInvoiceDTO, Integer> {
    private static final DetailInvoiceBUS INSTANCE = new DetailInvoiceBUS();

    public static DetailInvoiceBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<DetailInvoiceDTO> getAll() {
        return DetailInvoiceDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(DetailInvoiceDTO obj) {
        return obj.getInvoiceId();
    }

    public boolean delete(Integer id, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.INVOICE_DETAILINVOICE_SERVICE || id == null || id <= 0)
            return false;

        if (!DetailInvoiceDAL.getInstance().deleteAllDetailInvoiceByInvoiceId(id)) {
            return false;
        }

        return true;
    }

    public BUSResult getAllDetailInvoiceByInvoiceId(int invoiceId) {
        if (invoiceId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS, new ArrayList<>());
        ArrayList<DetailInvoiceDTO> detailInvoices = DetailInvoiceDAL.getInstance()
                .getAllDetailInvoiceByInvoiceId(invoiceId);
        if (detailInvoices.isEmpty())
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND, new ArrayList<>());
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.INVOICE_DETAIL_LOAD_SUCCESS, detailInvoices);
    }

    public boolean createDetailInvoiceByInvoiceId(int invoiceId, int employee_roleId, ArrayList<DetailInvoiceDTO> list,
            ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.INVOICE_DETAILINVOICE_SERVICE || list == null || list.isEmpty()
                || invoiceId <= 0)
            return false;

        if (!DetailInvoiceDAL.getInstance().insertAllDetailInvoiceByInvoiceId(invoiceId, list)) {
            return false;
        }
        ArrayList<DetailInvoiceDTO> newDetailInvoice = DetailInvoiceDAL.getInstance()
                .getAllDetailInvoiceByInvoiceId(invoiceId);

        return true;
    }

    public boolean insertRollbackDetailInvoice(ArrayList<DetailInvoiceDTO> list, int employee_roleId,
            ServiceAccessCode codeAccess,
            int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.INVOICE_DETAILINVOICE_SERVICE || list == null || list.isEmpty())
            return false;

        if (!DetailInvoiceDAL.getInstance().insertAllDetailInvoiceByInvoiceId(list.get(0).getInvoiceId(), list)) {
            return false;
        }
        ArrayList<DetailInvoiceDTO> newDetailInvoice = DetailInvoiceDAL.getInstance()
                .getAllDetailInvoiceByInvoiceId(list.get(0).getInvoiceId());

        return true;
    }

    @Override
    public DetailInvoiceDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return DetailInvoiceDAL.getInstance().getById(id);
    }
}
