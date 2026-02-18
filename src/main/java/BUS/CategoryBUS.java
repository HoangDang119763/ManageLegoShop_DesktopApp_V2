package BUS;

import DAL.CategoryDAL;
import DTO.BUSResult;
import DTO.CategoryDTO;
import DTO.CategoryDisplayDTO;
import DTO.PagedResponse;
import DTO.StatusDTO;
import ENUM.BUSOperationResult;
import ENUM.Status;
import ENUM.StatusType;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.ArrayList;

public class CategoryBUS extends BaseBUS<CategoryDTO, Integer> {
    private static final CategoryBUS INSTANCE = new CategoryBUS();

    private CategoryBUS() {
    }

    public static CategoryBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<CategoryDTO> getAll() {
        return CategoryDAL.getInstance().getAll();
    }

    @Override
    public CategoryDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return CategoryDAL.getInstance().getById(id);
    }

    @Override
    protected Integer getKey(CategoryDTO obj) {
        return obj.getId();
    }

    public int nextId() {
        return CategoryDAL.getInstance().getLastIdEver() + 1;
    }
    // --- NGHIỆP VỤ CHÍNH ---

    // Trong CategoryBUS.java
    public BUSResult insert(CategoryDTO obj) {
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 1. CHUẨN HÓA & VALIDATE
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        if (!isValidCategoryInput(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        if (!StatusBUS.getInstance().isValidStatusIdForType(StatusType.CATEGORY, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        if (CategoryDAL.getInstance().existsByName(obj.getName(), -1))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.CATEGORY_ADD_DUPLICATE);

        // 3. GHI DB (Gọi DB trực tiếp, không cache)
        if (!CategoryDAL.getInstance().insert(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CATEGORY_ADD_SUCCESS);
    }

    public BUSResult update(CategoryDTO obj) {
        if (obj == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        if (obj.getId() == 1) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.CATEGORY_CANNOT_UPDATE_SYSTEM);
        }
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.CATEGORY, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        // 1. Chuẩn hóa
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        // 3. Validate đầu vào
        if (!isValidCategoryInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 4. Kiểm tra tồn tại category này chưa
        if (CategoryDAL.getInstance().existsByName(obj.getName(), obj.getId())) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.CATEGORY_UPDATE_DUPLICATE);
        }
        // 6. Ghi Database
        if (!CategoryDAL.getInstance().update(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CATEGORY_UPDATE_SUCCESS);
    }

    public BUSResult delete(int id) {
        if (id <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        if (id == 1) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.CATEGORY_CANNOT_DELETE_SYSTEM);
        }

        // 3. Kiểm tra thực thể có tồn tại trong DB không
        CategoryDTO target = getById(id);
        if (target == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }

        // 4. Kiểm tra ràng buộc dữ liệu (Sản phẩm có đang dùng thể loại này không?)
        boolean hasProducts = ProductBUS.getInstance().isCategoryInAnyProduct(id);

        boolean success;
        if (hasProducts) {
            // --- XỬ LÝ XÓA MỀM (SOFT DELETE) ---
            // Lấy ID status INACTIVE từ DB
            StatusDTO inactiveStatus = StatusBUS.getInstance().getByTypeAndStatusName(StatusType.CATEGORY,
                    Status.Category.INACTIVE);
            if (inactiveStatus == null) {
                return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
            }
            success = CategoryDAL.getInstance().updateStatus(id, inactiveStatus.getId());
        } else {
            // --- XỬ LÝ XÓA CỨNG (HARD DELETE) ---
            // Xóa hoàn toàn vì không có sản phẩm nào bị ảnh hưởng
            success = CategoryDAL.getInstance().delete(id);
        }

        // 5. Kiểm tra kết quả thực thi Database
        if (!success) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        // 8. Trả về kết quả thành công
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CATEGORY_DELETE_SUCCESS);
    }

    private boolean isValidCategoryInput(CategoryDTO obj) {
        if (obj.getName() == null || obj.getName().trim().isEmpty())
            return false;
        // Giới hạn 100 ký tự cho tên thể loại
        return ValidationUtils.getInstance().validateVietnameseText100(obj.getName());
    }

    // --- SEARCH & FILTER ---

    public ArrayList<CategoryDTO> filterCategories(String keyword, int status, int page, int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();

        int finalStatus = (status <= 0) ? -1 : status;

        int finalPage = Math.max(1, page);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;

        return CategoryDAL.getInstance().getFiltered(cleanKeyword, finalStatus, finalPage, finalPageSize);
    }

    public boolean isCategoryActive(int categoryId) {
        StatusDTO activeStatus = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.CATEGORY, Status.Category.ACTIVE);
        if (activeStatus == null)
            return false;

        return CategoryDAL.getInstance().existsByIdAndStatus(categoryId, activeStatus.getId());
    }

    /**
     * Lọc danh mục cho màn hình quản lý (với statusDescription từ JOIN)
     * Tránh gọi BUS lẻ cho mỗi dòng bảng
     */
    public BUSResult filterCategoriesPagedForManageDisplay(String keyword, int statusId, int pageIndex, int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int finalStatusId = (statusId <= 0) ? -1 : statusId;
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;

        PagedResponse<CategoryDisplayDTO> pagedResponse = CategoryDAL.getInstance()
                .filterCategoriesPagedDisplay(cleanKeyword, finalStatusId, finalPageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedResponse);
    }

    public int getNextPotentialId() {
        return CategoryDAL.getInstance().getMaxId() + 1;
    }
}