package BUS;

import DAL.DetailInvoiceDAL;
import DTO.BUSResult;
import DTO.DetailInvoiceDTO;
import ENUM.BUSOperationResult;
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

    public BUSResult getAllDetailInvoiceByInvoiceId(int invoiceId) {
        if (invoiceId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS, new ArrayList<>());
        ArrayList<DetailInvoiceDTO> detailInvoices = DetailInvoiceDAL.getInstance()
                .getAllDetailInvoiceByInvoiceId(invoiceId);
        if (detailInvoices.isEmpty())
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND, new ArrayList<>());
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.INVOICE_DETAIL_LOAD_SUCCESS, detailInvoices);
    }

    @Override
    public DetailInvoiceDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return DetailInvoiceDAL.getInstance().getById(id);
    }
}
