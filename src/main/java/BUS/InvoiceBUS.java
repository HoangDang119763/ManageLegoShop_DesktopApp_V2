package BUS;

import DAL.InvoiceDAL;
import DTO.InvoiceDTO;
import DTO.DetailInvoiceDTO;
import ENUM.*;
import SERVICE.AuthorizationService;
import UTILS.ValidationUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Objects;

public class InvoiceBUS extends BaseBUS<InvoiceDTO, Integer> {
    private static final InvoiceBUS INSTANCE = new InvoiceBUS();

    public static InvoiceBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<InvoiceDTO> getAll() {
        return InvoiceDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(InvoiceDTO obj) {
        return obj.getId();
    }

    public boolean delete(Integer id, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.INVOICE_DETAILINVOICE_SERVICE || id == null || id <= 0)
            return false;
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 14))
            return false;

        if (!InvoiceDAL.getInstance().delete(id)) {
            return false;
        }
        arrLocal.removeIf(role -> Objects.equals(role.getId(), id));
        return true;
    }

    public boolean insert(InvoiceDTO obj, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.INVOICE_DETAILINVOICE_SERVICE || obj == null)
            return false;
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 13)
                || !isValidateInvoiceInput(obj))
            return false;

        obj.setCreateDate(LocalDateTime.now());
        if (!InvoiceDAL.getInstance().insert(obj))
            return false;
        arrLocal.add(new InvoiceDTO(obj));
        return true;
    }

    private boolean isValidateInvoiceInput(InvoiceDTO obj) {
        if (obj.getEmployeeId() <= 0 || obj.getCustomerId() <= 0)
            return false;

        ValidationUtils validator = ValidationUtils.getInstance();

        // Kiểm tra giá trị số hợp lệ
        if (!validator.validateBigDecimal(obj.getTotalPrice(), 12, 2, false)
                || !validator.validateBigDecimal(obj.getDiscountAmount(), 10, 2, false)) {
            return false;
        }

        // Nếu có discountCode, kiểm tra độ dài
        if (obj.getDiscountCode() != null && !validator.validateStringLength(obj.getDiscountCode(), 50)) {
            return false;
        }

        // Nếu discountAmount > 0 nhưng không có mã giảm giá => sai
        return obj.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0 || obj.getDiscountCode() != null;
    }

    public ArrayList<InvoiceDTO> filterInvoicesAdvance(
            String employeeId, String customerId, String discountCode,
            LocalDate startDate, LocalDate endDate,
            BigDecimal startTotalPrice, BigDecimal endTotalPrice) {
        ArrayList<InvoiceDTO> filteredList = new ArrayList<>();

        for (InvoiceDTO invoice : arrLocal) {
            LocalDate invoiceDate = invoice.getCreateDate().toLocalDate();

            // --- Kiểm tra ngày bắt buộc ---
            boolean matchesDate = startDate == null || !invoiceDate.isBefore(startDate);
            if (endDate != null && invoiceDate.isAfter(endDate)) {
                matchesDate = false;
            }
            if (!matchesDate) {
                continue; // Ngày không hợp lệ thì bỏ qua
            }

            // --- Kiểm tra các điều kiện còn lại ---
            boolean matchesAnyCondition = false;

            if (employeeId != null && !employeeId.isEmpty()) {
                if (String.valueOf(invoice.getEmployeeId()).equalsIgnoreCase(employeeId)) {
                    matchesAnyCondition = true;
                }
            }

            if (customerId != null && !customerId.isEmpty()) {
                if (String.valueOf(invoice.getCustomerId()).equalsIgnoreCase(customerId)) {
                    matchesAnyCondition = true;
                }
            }

            if (discountCode != null && !discountCode.isEmpty()) {
                if (invoice.getDiscountCode() != null && invoice.getDiscountCode().equalsIgnoreCase(discountCode)) {
                    matchesAnyCondition = true;
                }
            }

            if (startTotalPrice != null || endTotalPrice != null) {
                BigDecimal totalPrice = invoice.getTotalPrice();
                boolean matchPrice = startTotalPrice == null || totalPrice.compareTo(startTotalPrice) >= 0;
                if (endTotalPrice != null && totalPrice.compareTo(endTotalPrice) > 0) {
                    matchPrice = false;
                }
                if (matchPrice) {
                    matchesAnyCondition = true;
                }
            }

            // Nếu không nhập gì cả, thì mặc định là "thỏa"
            boolean hasAnyCondition = (employeeId != null && !employeeId.isEmpty()) ||
                    (customerId != null && !customerId.isEmpty()) ||
                    (discountCode != null && !discountCode.isEmpty()) ||
                    (startTotalPrice != null) || (endTotalPrice != null);

            if (!hasAnyCondition || matchesAnyCondition) {
                filteredList.add(new InvoiceDTO(invoice));
            }
        }

        return filteredList;
    }

    public ArrayList<InvoiceDTO> filterInvoicesByDiscountCode(String discountCode) {
        ArrayList<InvoiceDTO> result = new ArrayList<>();
        if (discountCode == null || discountCode.isEmpty())
            return result;

        for (InvoiceDTO invoice : arrLocal) {
            if (Objects.equals(invoice.getDiscountCode(), discountCode)) {
                result.add(new InvoiceDTO(invoice));
            }
        }
        return result;
    }

    /**
     * Kiểm tra xem sản phẩm có trong hóa đơn có trạng thái COMPLETED hay không
     *
     * @param productId ID của sản phẩm
     * @return true nếu sản phẩm có trong invoice hoàn thành
     */
    public boolean isProductInCompletedInvoice(String productId) {
        InvoiceBUS invoiceBUS = InvoiceBUS.getInstance();
        DetailInvoiceBUS detailInvoiceBUS = DetailInvoiceBUS.getInstance();

        // Đảm bảo dữ liệu đã được load
        if (invoiceBUS.isLocalEmpty()) {
            invoiceBUS.loadLocal();
        }
        if (detailInvoiceBUS.isLocalEmpty()) {
            detailInvoiceBUS.loadLocal();
        }

        // Lấy statusId của invoice COMPLETED
        int completedStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusNameLocal(StatusType.INVOICE, Status.Invoice.COMPLETED).getId();

        if (completedStatusId <= 0) {
            return false; // Không tìm thấy status COMPLETED
        }

        // Kiểm tra tất cả DetailInvoice
        for (DetailInvoiceDTO detail : detailInvoiceBUS.getAllLocal()) {
            if (detail.getProductId().equals(productId)) {
                // Tìm invoice tương ứng
                InvoiceDTO invoice = invoiceBUS.getByIdLocal(detail.getInvoiceId());
                if (invoice != null && invoice.getStatusId() == completedStatusId) {
                    return true; // Sản phẩm này trong invoice COMPLETED
                }
            }
        }

        return false;
    }

}
