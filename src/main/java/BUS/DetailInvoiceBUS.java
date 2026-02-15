package BUS;

import DAL.DetailInvoiceDAL;
import DTO.DetailInvoiceDTO;
import ENUM.ServiceAccessCode;

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

    public ArrayList<DetailInvoiceDTO> getAllDetailInvoiceByInvoiceId(int invoiceId) {
        if (invoiceId <= 0)
            return null;
        return DetailInvoiceDAL.getInstance().getAllDetailInvoiceByInvoiceId(invoiceId);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }
}
