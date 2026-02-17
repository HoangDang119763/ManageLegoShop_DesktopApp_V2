package BUS;

import DAL.CustomerDAL;
import DTO.BUSResult;
import DTO.CustomerDTO;
import DTO.CustomerDisplayDTO;
import DTO.PagedResponse;
import ENUM.*;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.ArrayList;

public class CustomerBUS extends BaseBUS<CustomerDTO, Integer> {
    private static final CustomerBUS INSTANCE = new CustomerBUS();

    private CustomerBUS() {
    }

    public static CustomerBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<CustomerDTO> getAll() {
        return CustomerDAL.getInstance().getAll();
    }

    @Override
    public CustomerDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return CustomerDAL.getInstance().getById(id);
    }

    @Override
    protected Integer getKey(CustomerDTO obj) {
        return obj.getId();
    }

    public BUSResult delete(int id) {
        // 1. Kiểm tra đầu vào cơ bản
        if (id <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        // 2. Bảo vệ khách hàng hệ thống (ID 1: Khách vãng lai/Mặc định)
        if (id == 1) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.CUSTOMER_CANNOT_DELETE_SYSTEM);
        }

        // 3. Kiểm tra thực thể có tồn tại trong DB không
        CustomerDTO targetCustomer = getById(id);
        if (targetCustomer == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }

        // 4. Lấy ID trạng thái INACTIVE để so sánh và sử dụng
        int inactiveStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.CUSTOMER, Status.Customer.INACTIVE).getId();

        // 5. Kiểm tra ràng buộc dữ liệu (Quyết định Xóa mềm hay Xóa cứng)
        boolean hasInvoice = InvoiceBUS.getInstance().isCustomerInAnyInvoice(id);

        // --- CHỐT CHẶN: XỬ LÝ KHI ĐÃ INACTIVE ---
        if (targetCustomer.getStatusId() == inactiveStatusId) {
            if (hasInvoice) {
                // Đã ẩn rồi và có lịch sử -> Trả về thành công luôn
                return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DATA_ALREADY_DELETED);
            }
            // Nếu đã Inactive mà không có hóa đơn (do dữ liệu rác) -> Cho phép rơi xuống
            // Xóa Cứng bên dưới
        }

        boolean success;
        if (hasInvoice) {
            // --- XỬ LÝ XÓA MỀM (SOFT DELETE) ---
            // Gọi DAL truyền trực tiếp ID và Status ID mới
            success = CustomerDAL.getInstance().updateStatus(id, inactiveStatusId);
        } else {
            // --- XỬ LÝ XÓA CỨNG (HARD DELETE) ---
            success = CustomerDAL.getInstance().delete(id);
        }

        // 6. Kiểm tra kết quả thực thi Database
        if (!success) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        // 7. Cập nhật Database (không cache)
        if (hasInvoice) {
            // Cập nhật trạng thái mới cho object trong DB
            targetCustomer.setStatusId(inactiveStatusId);
        } else {
            // Xóa hoàn toàn khỏi DB
            // Dữ liệu đã bị xóa ở bước 6
        }

        // 8. Trả về kết quả thành công
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CUSTOMER_DELETE_SUCCESS);
    }

    public BUSResult insert(CustomerDTO obj) {
        if (obj == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setAddress(validate.convertEmptyStringToNull(obj.getAddress()));
        obj.setFirstName(validate.normalizeWhiteSpace(obj.getFirstName()));
        obj.setLastName(validate.normalizeWhiteSpace(obj.getLastName()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));
        if (!isValidCustomerInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // Đảm bảo trạng thái có id hợp lệ
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.CUSTOMER, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        if (isExistCustomer(obj, -1)) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.CUSTOMER_ADD_DUPLICATE);
        }

        if (!CustomerDAL.getInstance().insert(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CUSTOMER_ADD_SUCCESS);
    }

    // Kiểm tra đầu vào Từ UI gửi xuống BUS (insert/update)
    private boolean isValidCustomerInput(CustomerDTO obj) {
        // 1. Kiểm tra các trường bắt buộc (Không null và không rỗng)
        if (obj.getFirstName() == null || obj.getFirstName().trim().isEmpty() ||
                obj.getLastName() == null || obj.getLastName().trim().isEmpty() ||
                obj.getPhone() == null || obj.getPhone().trim().isEmpty()) {
            return false;
        }

        ValidationUtils validator = ValidationUtils.getInstance();

        // 3. Kiểm tra tính hợp lệ của Địa chỉ (Nếu có)
        if (obj.getAddress() != null && !validator.validateVietnameseText255(obj.getAddress())) {
            return false;
        }

        if (obj.getDateOfBirth() != null && !validator.validateDateOfBirth(obj.getDateOfBirth())) {
            return false;
        }

        // 5. Kiểm tra các trường chính bằng Validator
        return validator.validateVietnameseText100(obj.getFirstName().trim()) &&
                validator.validateVietnameseText100(obj.getLastName().trim()) &&
                validator.validateVietnamesePhoneNumber(obj.getPhone().trim());
    }

    public BUSResult update(CustomerDTO obj) {
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 5. Kiểm tra Status
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.CUSTOMER, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        // 1. Chuẩn hóa dữ liệu NGAY LẬP TỨC để các bước check sau chính xác 100%
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setAddress(validate.convertEmptyStringToNull(obj.getAddress()));
        obj.setFirstName(validate.normalizeWhiteSpace(obj.getFirstName()));
        obj.setLastName(validate.normalizeWhiteSpace(obj.getLastName()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));

        // 2. Validate định dạng
        if (!isValidCustomerInput(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        // 4. Kiểm tra xem dữ liệu mới có bị trùng với "khách hàng khác" không
        if (isExistCustomer(obj, obj.getId()))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.CUSTOMER_UPDATE_DUPLICATE);

        // 6. Ghi vào Database (không cache)
        if (!CustomerDAL.getInstance().update(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CUSTOMER_UPDATE_SUCCESS);
    }

    // Kiểm tra xem khách hàng có tồn tại với dữ liệu tương tự không (call DB)
    public boolean isExistCustomer(CustomerDTO obj, int currentId) {
        if (obj.getFirstName() == null || obj.getLastName() == null || obj.getPhone() == null)
            return false;

        return CustomerDAL.getInstance().existsByCustomerData(
                obj.getFirstName(),
                obj.getLastName(),
                obj.getPhone(),
                obj.getDateOfBirth(),
                obj.getAddress(),
                currentId);
    }

    public ArrayList<CustomerDTO> searchCustomerByPhone(String phone) {
        // Trả về danh sách rỗng thay vì null để UI không bị NullPointerException
        if (phone == null || phone.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return CustomerDAL.getInstance().searchByPhone(phone.trim());
    }

    public BUSResult filterCustomersPagedForManageDisplay(
            String keyword, int statusId, int pageIndex, int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int finalStatusId = (statusId <= 0) ? -1 : statusId;
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        PagedResponse<CustomerDisplayDTO> pagedData = CustomerDAL.getInstance()
                .filterCustomersPagedForManageDisplay(cleanKeyword, finalStatusId, finalPageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }
}
