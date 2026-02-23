package BUS;

import DAL.SupplierDAL;
import DTO.BUSResult;
import DTO.SupplierDTO;
import DTO.SupplierDisplayDTO;
import DTO.SupplierForImportDTO;
import ENUM.BUSOperationResult;
import ENUM.Status;
import ENUM.StatusType;
import DTO.PagedResponse;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.ArrayList;

public class SupplierBUS extends BaseBUS<SupplierDTO, Integer> {
    private static final SupplierBUS INSTANCE = new SupplierBUS();

    private SupplierBUS() {
    }

    public static SupplierBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<SupplierDTO> getAll() {
        return SupplierDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(SupplierDTO obj) {
        return obj.getId();
    }

    public int nextId() {
        return SupplierDAL.getInstance().getLastIdEver() + 1;
    }

    public BUSResult delete(int id) {
        // 1.Kiểm tra null
        if (id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        SupplierDTO targetSupplier = getById(id);
        if (targetSupplier == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }

        // 4. Lấy ID trạng thái INACTIVE để so sánh và sử dụng
        int inactiveStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.SUPPLIER, Status.Supplier.INACTIVE).getId();

        // 5. Kiểm tra ràng buộc dữ liệu (Quyết định Xóa mềm hay Xóa cứng)
        boolean hasImport = ImportBUS.getInstance().isSupplierInAnyImport(id);
        // --- CHỐT CHẶN: XỬ LÝ KHI ĐÃ INACTIVE ---
        if (targetSupplier.getStatusId() == inactiveStatusId) {
            if (hasImport) {
                // Đã ẩn rồi và có lịch sử -> Trả về thành công luôn
                return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DATA_ALREADY_DELETED);
            }
            // Nếu đã Inactive mà không có hóa đơn (do dữ liệu rác) -> Cho phép rơi xuống
            // Xóa Cứng bên dưới
        }

        boolean success;
        if (hasImport) {
            // --- XỬ LÝ XÓA MỀM (SOFT DELETE) ---
            // Gọi DAL truyền trực tiếp ID và Status ID mới
            success = SupplierDAL.getInstance().updateStatus(id, inactiveStatusId);
        } else {
            // --- XỬ LÝ XÓA CỨNG (HARD DELETE) ---
            success = SupplierDAL.getInstance().delete(id);
        }

        // 6. Kiểm tra kết quả thực thi Database
        if (!success) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        // 8. Trả về kết quả thành công
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CUSTOMER_DELETE_SUCCESS);
    }

    private boolean isValidSupplierInput(SupplierDTO obj) {
        if (obj.getName() == null || obj.getName().trim().isEmpty() ||
                obj.getPhone() == null || obj.getPhone().trim().isEmpty() ||
                obj.getAddress() == null || obj.getAddress().trim().isEmpty())
            return false;

        ValidationUtils validator = ValidationUtils.getInstance();
        return validator.validateVietnameseText50(obj.getName()) &&
                validator.validateVietnamesePhoneNumber(obj.getPhone()) &&
                validator.validateVietnameseText65k4(obj.getAddress()) &&
                (obj.getEmail() == null || obj.getEmail().isEmpty() || validator.validateEmail(obj.getEmail()));
    }

    public BUSResult insert(SupplierDTO obj) {
        // 1. Kiểm tra ID hợp lệ
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setEmail(validate.convertEmptyStringToNull(obj.getEmail()));
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        obj.setEmail(validate.normalizeWhiteSpace(obj.getEmail()));
        if (!isValidSupplierInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }
        // Đảm bảo trạng thái có id hợp lệ
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.SUPPLIER, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        if (isExistsSupplier(obj, -1)) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.SUPPLIER_ADD_DUPLICATE);
        }

        if (!SupplierDAL.getInstance().insert(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.SUPPLIER_ADD_SUCCESS);
    }

    public BUSResult update(SupplierDTO obj) {
        // 1. Kiểm tra null & phân quyền
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 2. Kiểm tra Status
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.SUPPLIER, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);
        // 3. Kiểm tra đầu vào hợp lệ khi truyền xuống CSDL
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setEmail(validate.convertEmptyStringToNull(obj.getEmail()));
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        obj.setPhone(validate.normalizeWhiteSpace(obj.getPhone()));
        obj.setAddress(validate.normalizeWhiteSpace(obj.getAddress()));
        obj.setEmail(validate.normalizeWhiteSpace(obj.getEmail()));
        if (!isValidSupplierInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        if (isExistsSupplier(obj, obj.getId())) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.SUPPLIER_UPDATE_DUPLICATE);
        }
        // 6. Kiểm tra thêm vào CSDL
        if (!SupplierDAL.getInstance().update(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.SUPPLIER_UPDATE_SUCCESS);
    }

    public boolean isExistsSupplier(SupplierDTO obj, int currentId) {
        if (obj.getName() == null || obj.getName().trim().isEmpty() ||
                obj.getPhone() == null || obj.getPhone().trim().isEmpty() ||
                obj.getAddress() == null || obj.getAddress().trim().isEmpty())
            return false;
        return SupplierDAL.getInstance().existsBySupplierData(obj.getName(), obj.getPhone(), obj.getAddress(),
                obj.getEmail(), currentId);
    }

    public BUSResult filterSuppliersPagedForManageDisplay(
            String keyword, int statusId, int pageIndex, int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int finalStatusId = (statusId <= 0) ? -1 : statusId;
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;

        // Gọi DAL với JOIN để lấy dữ liệu hoàn chỉnh
        PagedResponse<SupplierDisplayDTO> pagedData = SupplierDAL.getInstance()
                .filterSuppliersPagedForManageDisplay(cleanKeyword, finalStatusId, finalPageIndex,
                        finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    public BUSResult filterSuppliersByKeywordForImport(String keyword) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int activeStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.SUPPLIER, Status.Supplier.ACTIVE).getId();
        ArrayList<SupplierForImportDTO> suppliers = SupplierDAL.getInstance()
                .filterSuppliersByKeywordForImport(cleanKeyword, activeStatusId);
        if (suppliers == null) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
        return new BUSResult(BUSOperationResult.SUCCESS, null, suppliers);
    }

    @Override
    public SupplierDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return SupplierDAL.getInstance().getById(id);
    }
}
