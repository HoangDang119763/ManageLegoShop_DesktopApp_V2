package BUS;

import DAL.CategoryDAL;
import DTO.BUSResult;
import DTO.CategoryDTO;
import ENUM.BUSOperationResult;
import ENUM.Status;
import ENUM.StatusType;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.ArrayList;
import java.util.Objects;

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
    protected Integer getKey(CategoryDTO obj) {
        return obj.getId();
    }

    public String nextId() {
        return String.valueOf(arrLocal.size() + 1);
    }

    // --- NGHIỆP VỤ CHÍNH ---

    // Trong CategoryBUS.java
    public BUSResult insert(CategoryDTO obj) {
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 1. CHUẨN HÓA TRƯỚC
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        // 2. KIỂM TRA ĐẦU VÀO & STATUS (Gộp lại cho gọn)
        if (!isValidCategoryInput(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        if (!StatusBUS.getInstance().isValidStatusIdForType(StatusType.CATEGORY, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        // 3. KIỂM TRA TRÙNG TÊN
        if (isExistCategory(-1, obj.getName()))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.CATEGORY_ADD_DUPLICATE);

        // 4. GHI DB & CACHE
        if (!CategoryDAL.getInstance().insert(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        addToLocalCache(obj);
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CATEGORY_ADD_SUCCESS);
    }

    public BUSResult update(CategoryDTO obj) {
        if (obj == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        // Bảo vệ Category gốc (ví dụ ID 1 là "Chưa phân loại")
        if (obj.getId() == 1) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.CATEGORY_CANNOT_UPDATE_SYSTEM);
        }
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.CATEGORY, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        // 1. Chuẩn hóa
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        // 2. Kiểm tra không thay đổi dữ liệu (Tránh ghi DB thừa)
        if (isDataUnchanged(obj)) {
            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CATEGORY_UPDATE_SUCCESS);
        }

        // 3. Validate đầu vào
        if (!isValidCategoryInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 4. Kiểm tra tồn tại category này chưa
        if (isExistCategory(obj.getId(), obj.getName())) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.CATEGORY_UPDATE_DUPLICATE);
        }

        // 5. Ghi Database
        if (!CategoryDAL.getInstance().update(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        // 6. Cập nhật Cache
        updateLocalCache(obj);
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CATEGORY_UPDATE_SUCCESS);
    }

    public BUSResult delete(int id) {
        // 1. Kiểm tra đầu vào cơ bản
        if (id <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        // 2. Bảo vệ Category hệ thống (ID 1: Thường là "Chưa phân loại" hoặc "Mặc
        // định")
        if (id == 1) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.CATEGORY_CANNOT_DELETE_SYSTEM);
        }

        // 3. Kiểm tra thực thể có tồn tại trong Cache không
        CategoryDTO target = getByIdLocal(id);
        if (target == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }

        // 4. Lấy ID trạng thái INACTIVE để so sánh và sử dụng
        int inactiveStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusNameLocal(StatusType.CATEGORY, Status.Category.INACTIVE).getId();

        // 5. Kiểm tra ràng buộc dữ liệu (Sản phẩm có đang dùng thể loại này không?)
        boolean hasProducts = ProductBUS.getInstance().isCategoryInAnyProduct(id);

        // --- CHỐT CHẶN: XỬ LÝ KHI ĐÃ INACTIVE ---
        if (target.getStatusId() == inactiveStatusId) {
            if (hasProducts) {
                // Đã Inactive và có sản phẩm -> Mục tiêu "ẩn" đã đạt được, trả về success luôn
                return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DATA_ALREADY_DELETED);
            }
            // Nếu đã Inactive mà chưa có sản phẩm nào dùng -> Cho phép rơi xuống Xóa Cứng
            // để dọn DB
        }

        boolean success;
        if (hasProducts) {
            // --- XỬ LÝ XÓA MỀM (SOFT DELETE) ---
            // Gọi DAL truyền ID và Status ID mới trực tiếp
            success = CategoryDAL.getInstance().updateStatus(id, inactiveStatusId);
        } else {
            // --- XỬ LÝ XÓA CỨNG (HARD DELETE) ---
            // Xóa hoàn toàn vì không có sản phẩm nào bị ảnh hưởng
            success = CategoryDAL.getInstance().delete(id);
        }

        // 6. Kiểm tra kết quả thực thi Database
        if (!success) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        // 7. ĐỒNG BỘ CACHE LOCAL (Chỉ chạy khi DB đã thành công)
        if (hasProducts) {
            // Cập nhật trạng thái mới cho object trong bộ nhớ
            target.setStatusId(inactiveStatusId);
            updateLocalCache(target);
        } else {
            // Xóa hoàn toàn khỏi Map và List local
            arrLocal.remove(target);
            mapLocal.remove(id);
        }

        // 8. Trả về kết quả thành công
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.CATEGORY_DELETE_SUCCESS);
    }

    // --- HÀM HỖ TRỢ (PRIVATE/PROTECTED) ---

    private void addToLocalCache(CategoryDTO obj) {
        CategoryDTO newObj = new CategoryDTO(obj);
        mapLocal.put(newObj.getId(), newObj);
        arrLocal.add(newObj);
    }

    private void updateLocalCache(CategoryDTO obj) {
        CategoryDTO newObj = new CategoryDTO(obj);
        mapLocal.put(newObj.getId(), newObj);
        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), obj.getId())) {
                arrLocal.set(i, newObj);
                break;
            }
        }
    }

    public boolean isExistCategory(int id, String name) {
        if (name == null || name.trim().isEmpty())
            return false;

        String normalizedName = name.trim();
        for (CategoryDTO cate : arrLocal) {
            if (cate.getId() != id &&
                    cate.getName().equalsIgnoreCase(normalizedName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDataUnchanged(CategoryDTO obj) {
        CategoryDTO existing = getByIdLocal(obj.getId());
        return existing != null &&
                Objects.equals(existing.getName(), obj.getName()) &&
                Objects.equals(existing.getStatusId(), obj.getStatusId());
    }

    private boolean isValidCategoryInput(CategoryDTO obj) {
        if (obj.getName() == null || obj.getName().trim().isEmpty())
            return false;
        // Giới hạn 100 ký tự cho tên thể loại
        return ValidationUtils.getInstance().validateVietnameseText100(obj.getName());
    }

    // --- SEARCH & FILTER ---

    public ArrayList<CategoryDTO> filterCategories(String searchBy, String keyword, int statusFilter) {
        ArrayList<CategoryDTO> filteredList = new ArrayList<>();
        String finalKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();

        for (CategoryDTO cate : arrLocal) {
            boolean matchesStatus = (statusFilter == -1) || (cate.getStatusId() == statusFilter);
            boolean matchesSearch = true;

            if (!finalKeyword.isEmpty()) {
                String idStr = String.valueOf(cate.getId());
                String name = (cate.getName() != null) ? cate.getName().toLowerCase() : "";

                switch (searchBy) {
                    case "Mã thể loại" -> matchesSearch = idStr.contains(finalKeyword);
                    case "Tên thể loại" -> matchesSearch = name.contains(finalKeyword);
                }
            }

            if (matchesSearch && matchesStatus) {
                filteredList.add(cate);
            }
        }
        return filteredList;
    }

    /**
     * Dùng để kiểm tra nhanh một Category ID có hợp lệ và đang ACTIVE không
     */
    public boolean isValidCategory(int categoryId) {
        CategoryDTO temp = getByIdLocal(categoryId);
        if (temp == null)
            return false;

        int activeStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusNameLocal(StatusType.CATEGORY, Status.Category.ACTIVE).getId();

        return temp.getStatusId() == activeStatusId;
    }
}