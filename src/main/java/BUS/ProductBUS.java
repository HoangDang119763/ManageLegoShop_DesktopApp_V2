package BUS;

import DAL.ProductDAL;
import DTO.ProductDTO;
import DTO.ProductDisplayDTO;
import DTO.ProductDisplayForImportDTO;
import DTO.ProductDisplayForSellingDTO;
import DTO.BUSResult;
import DTO.PagedResponse;
import ENUM.*;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ProductBUS extends BaseBUS<ProductDTO, String> {
    private static final ProductBUS INSTANCE = new ProductBUS();

    private ProductBUS() {
    }

    public static ProductBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<ProductDTO> getAll() {
        return ProductDAL.getInstance().getAll();
    }

    @Override
    protected String getKey(ProductDTO obj) {
        return obj.getId();
    }

    @Override
    public ProductDTO getById(String id) {
        if (id == null || id.isEmpty())
            return null;
        return ProductDAL.getInstance().getById(id);
    }

    // [STATELESS] --- LOGIC XỬ LÝ ID AN TOÀN ---
    // Auto-generate next product ID by querying database (no local cache)
    public String autoId() {
        return ProductDAL.getInstance().getNextProductId();
    }

    private String nextProductId(String currentId) {
        int temp = Integer.parseInt(currentId.substring(2)) + 1;
        return String.format("SP%05d", temp);
    }

    // --- NGHIỆP VỤ CHÍNH ---
    public BUSResult insert(ProductDTO obj) {
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 2. VALIDATE SAU
        if (!isValidProductInput(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        // Chuyển empty string thành null để sạch Database
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setDescription(validate.convertEmptyStringToNull(obj.getDescription()));
        obj.setImageUrl(validate.convertEmptyStringToNull(obj.getImageUrl()));
        // Validate categoryId tồn tại (nếu có set)
        CategoryBUS cateBus = CategoryBUS.getInstance();
        if (!cateBus.isCategoryActive(obj.getCategoryId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.PRODUCT_ADD_CATEGORY_INVALID);

        // Đảm bảo trạng thái có id hợp lệ
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.PRODUCT, obj.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        if (isExistProductName("", obj.getName()))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.PRODUCT_ADD_DUPLICATE);

        // 3. THIẾT LẬP MẶC ĐỊNH
        obj.setId(autoId());
        // 1. CHUẨN HÓA TRƯỚC (Rất quan trọng)
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        obj.setDescription(validate.normalizeWhiteSpace(obj.getDescription()));
        obj.setStockQuantity(0);
        obj.setImportPrice(BigDecimal.ZERO);
        LocalDateTime now = LocalDateTime.now();
        obj.setCreatedAt(now);
        obj.setUpdatedAt(now);

        if (!ProductDAL.getInstance().insert(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        // [STATELESS] No cache update needed - data will refresh from DB on next
        // getAll()
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_ADD_SUCCESS);
    }

    public BUSResult update(ProductDTO obj) {
        if (obj == null || obj.getId() == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // [STATELESS] Query database instead of local cache
        ProductDTO oldProduct = ProductDAL.getInstance().getById(obj.getId());
        if (oldProduct == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        int inactiveId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.PRODUCT, Status.Product.INACTIVE).getId();

        // 1. Check chuyển sang Inactive (Đồng bộ với logic Delete)
        if (oldProduct.getStatusId() != inactiveId && obj.getStatusId() == inactiveId) {
            BUSResult checkInactive = validateTransitionToInactive(oldProduct);
            if (checkInactive != null)
                return checkInactive;
        }

        // 2. Check thay đổi Category
        BUSResult checkCate = validateCategoryTransition(oldProduct.getCategoryId(), obj.getCategoryId());
        if (checkCate != null)
            return checkCate;

        // 3. Chuẩn hóa & Validate dữ liệu
        ValidationUtils val = ValidationUtils.getInstance();
        obj.setDescription(val.convertEmptyStringToNull(obj.getDescription()));
        obj.setImageUrl(val.convertEmptyStringToNull(obj.getImageUrl()));

        if (!isValidProductUpdate(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        if (isExistProductName(obj.getId(), obj.getName()))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.PRODUCT_UPDATE_DUPLICATE);

        obj.setName(val.normalizeWhiteSpace(obj.getName()));
        obj.setDescription(val.normalizeWhiteSpace(obj.getDescription()));
        obj.setCreatedAt(oldProduct.getCreatedAt()); // Giữ nguyên ngày sinh
        obj.setUpdatedAt(LocalDateTime.now());
        // 4. Lưu DB & Cache
        if (!ProductDAL.getInstance().update(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        // [STATELESS] No cache update needed - data will refresh from DB on next
        // getAll()
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_UPDATE_SUCCESS);
    }

    public BUSResult delete(String id) {
        // [STATELESS] Query database instead of local cache
        ProductDTO target = ProductDAL.getInstance().getById(id);
        if (target == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        int inactiveId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.PRODUCT, Status.Product.INACTIVE).getId();

        // 1. Check tồn kho trước khi cho phép "biến mất" hoặc "ngừng bán"
        BUSResult checkStock = validateTransitionToInactive(target);
        if (checkStock != null)
            return checkStock;

        // 2. Kiểm tra lịch sử
        boolean hasHistory = InvoiceBUS.getInstance().isProductInAnyInvoice(id) ||
                ImportBUS.getInstance().isProductInAnyImport(id);

        // 3. Nếu đã Inactive và có lịch sử -> Coi như đã xóa xong
        if (target.getStatusId() == inactiveId && hasHistory) {
            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DATA_ALREADY_DELETED);
        }

        // 4. Thực thi Xóa mềm hoặc Xóa cứng
        boolean success = hasHistory ? ProductDAL.getInstance().updateStatus(id, inactiveId)
                : ProductDAL.getInstance().delete(id);

        if (!success)
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_DELETE_SUCCESS);
    }

    // Kiểm tra điều kiện để một sản phẩm có thể chuyển sang Ngừng kinh doanh
    private BUSResult validateTransitionToInactive(ProductDTO product) {
        if (product.getStockQuantity() > 0) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.PRODUCT_DELETE_WITH_STOCK);
        }
        return null; // Null nghĩa là hợp lệ
    }

    // Kiểm tra tính hợp lệ của Category khi thay đổi
    private BUSResult validateCategoryTransition(int oldCateId, int newCateId) {
        if (oldCateId != newCateId) {
            if (!CategoryBUS.getInstance().isCategoryActive(newCateId)) {
                return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.PRODUCT_ADD_CATEGORY_INVALID);
            }
        }
        return null;
    }

    // --- EXCEL IMPORT (ĐÃ ÉP CHUẨN) ---
    public int insertListProductExcel(ArrayList<ProductDTO> listProducts) {
        if (listProducts == null || listProducts.isEmpty())
            return 2;

        ValidationUtils validate = ValidationUtils.getInstance();
        Set<String> excelNames = new HashSet<>();
        String nextId = autoId();

        // Lấy status mặc định cho hàng mới
        int activeStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.PRODUCT, Status.Product.ACTIVE).getId();

        for (ProductDTO p : listProducts) {
            // Normalize trước
            p.setName(validate.normalizeWhiteSpace(p.getName()));
            if (!isValidProductInputForExcel(p))
                return 3;

            // Xử lý trùng tên trong file hoặc hệ thống
            if (excelNames.contains(p.getName()) || isExistProductName("", p.getName())) {
                p.setName(p.getName() + " (" + nextId + ")");
            }
            excelNames.add(p.getName());

            if (!CategoryBUS.getInstance().isCategoryActive(p.getCategoryId()))
                return 4;

            // Gán các trường bắt buộc
            p.setId(nextId);
            p.setStatusId(activeStatusId);
            p.setStockQuantity(0);
            p.setImportPrice(BigDecimal.ZERO);
            // Note: sellingPrice từ Excel nếu có, nếu không excel cung cấp thì UI phải
            // validate

            nextId = nextProductId(nextId);
        }

        if (!ProductDAL.getInstance().insertListProductExcel(listProducts))
            return 7;

        // [STATELESS] No cache update needed - data will refresh from DB on next
        // getAll()
        return 1;
    }

    // [STATELESS] TỐI ƯU HIỆU NĂNG CACHE
    public boolean updateQuantitySellingPriceListProduct(ArrayList<ProductDTO> listProducts, boolean isAdd) {
        if (listProducts == null || listProducts.isEmpty())
            return false;

        // [STATELESS] Get fresh data from DB for calculation instead of map Local
        for (ProductDTO p : listProducts) {
            ProductDTO dbProduct = ProductDAL.getInstance().getById(p.getId());
            if (dbProduct == null)
                return false;

            int newQty = isAdd ? (dbProduct.getStockQuantity() + p.getStockQuantity())
                    : Math.max(0, dbProduct.getStockQuantity() - p.getStockQuantity());
            p.setStockQuantity(newQty);
        }

        if (!ProductDAL.getInstance().updateProductQuantityAndSellingPrice(listProducts))
            return false;

        // [STATELESS] No cache update needed - data will refresh from DB on next
        // getAll()
        return true;
    }

    // [STATELESS] HÀM HỖ TRỢ & VALIDATE
    private boolean isExistProductName(String id, String name) {
        // [STATELESS] Query database instead of iterating local cache
        return ProductDAL.getInstance().isProductNameExists(id, name);
    }

    public boolean isValidProductInput(ProductDTO obj) {
        if (obj.getName() == null || obj.getName().isEmpty())
            return false;
        if (obj.getSellingPrice() == null)
            return false;
        ValidationUtils v = ValidationUtils.getInstance();
        return v.validateVietnameseText255(obj.getName()) &&
                v.validateBigDecimal(obj.getSellingPrice(), 10, 2, false) &&
                (obj.getDescription() == null || v.validateVietnameseText65k4(obj.getDescription()));
    }

    private boolean isValidProductUpdate(ProductDTO obj) {
        // 1. Kiểm tra các trường bắt buộc không được null
        if (obj == null || obj.getId() == null || obj.getName() == null || obj.getSellingPrice() == null) {
            return false;
        }

        // 4. Validate bằng Regex/Rules thông qua ValidationUtils
        ValidationUtils v = ValidationUtils.getInstance();

        // Tên sản phẩm: Bắt buộc
        if (!v.validateVietnameseText255(obj.getName().trim())) {
            return false;
        }

        // Giá bán: Bắt buộc (10 chữ số, 2 số thập phân, không được âm)
        if (!v.validateBigDecimal(obj.getSellingPrice(), 10, 2, false)) {
            return false;
        }

        // Mô tả: Chỉ validate nếu có nội dung (đã xử lý null ở trên)
        if (obj.getDescription() != null && !v.validateVietnameseText65k4(obj.getDescription().trim())) {
            return false;
        }

        // Đường dẫn ảnh: Chỉ validate nếu có nội dung (đã xử lý null ở trên)
        if (obj.getImageUrl() != null && !v.validateVietnameseText255(obj.getImageUrl().trim())) {
            return false;
        }

        return true;
    }

    public boolean isValidProductInputForExcel(ProductDTO obj) {
        if (obj.getName() == null || obj.getName().isEmpty())
            return false;
        ValidationUtils v = ValidationUtils.getInstance();

        // SỬA LỖI LOGIC: Trả về false nếu validate thất bại (!)
        if (obj.getDescription() != null && !v.validateVietnameseText65k4(obj.getDescription()))
            return false;
        if (obj.getImageUrl() != null && !v.validateVietnameseText255(obj.getImageUrl()))
            return false;

        return v.validateVietnameseText255(obj.getName());
    }

    public boolean isDuplicateImageUrl(ProductDTO obj) {
        ProductDTO existingPro = ProductDAL.getInstance().getById(obj.getId());
        return existingPro != null &&
                Objects.equals(existingPro.getImageUrl(), obj.getImageUrl());
    }

    // [STATELESS] Filter products from fresh DB data
    public BUSResult filterProductsPagedForManage(String keyword, int categoryId, int statusId,
            BigDecimal startPrice, BigDecimal endPrice,
            int pageIndex, int pageSize) {

        // 1. Tiền xử lý (Ví dụ: mặc định inStockOnly = false hoặc lấy từ setting)
        boolean inStockOnly = false;

        // 2. Gọi DAL lấy dữ liệu phân trang
        PagedResponse<ProductDTO> pagedData = ProductDAL.getInstance()
                .filterProductsPaged(keyword, categoryId, statusId, startPrice, endPrice, inStockOnly, pageIndex,
                        pageSize);

        // 3. Trả về BUSResult (Đã tích hợp hàm getPagedData() ma thuật)
        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    /**
     * [OPTIMIZED] Filter products với category name & status description (JOIN -
     * không cần gọi DB lẻ)
     * Sử dụng cho display trong TableView
     */
    public BUSResult filterProductsPagedForManageDisplay(String keyword, int categoryId, int statusId,
            BigDecimal startPrice, BigDecimal endPrice,
            int pageIndex, int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int finalCategoryId = (categoryId <= 0) ? -1 : categoryId;
        int finalStatusId = (statusId <= 0) ? -1 : statusId;
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        BigDecimal finalStartPrice = (startPrice == null || startPrice.compareTo(BigDecimal.ZERO) < 0) ? null
                : startPrice;
        BigDecimal finalEndPrice = (endPrice == null || endPrice.compareTo(BigDecimal.ZERO) < 0) ? null : endPrice;
        boolean inStockOnly = false;

        // Gọi DAL với JOIN để lấy dữ liệu hoàn chỉnh
        PagedResponse<ProductDisplayDTO> pagedData = ProductDAL.getInstance()
                .filterProductsPagedForManageDisplay(cleanKeyword, finalCategoryId, finalStatusId, finalStartPrice,
                        finalEndPrice,
                        inStockOnly, finalPageIndex,
                        finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    // [STATELESS] Check if category is used in any product
    public boolean isCategoryInAnyProduct(int categoryId) {
        // [STATELESS] Query database instead of loading cache
        return ProductDAL.getInstance().isCategoryInUse(categoryId);
    }

    /**
     * Filter sản phẩm cho giao diện nhập hàng
     * Chỉ hiển thị sản phẩm có tồn kho > 0
     * 
     * @param keyword    Tìm kiếm theo tên sản phẩm
     * @param categoryId Lọc theo thể loại (-1 = tất cả)
     * @param pageIndex  Trang hiện tại (0-based)
     * @param pageSize   Số item/trang
     * @return BUSResult chứa PagedResponse<ProductDisplayForImportDTO>
     */
    public BUSResult filterProductsPagedForImport(String keyword, int categoryId, String priceOrder, int pageIndex,
            int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int finalCategoryId = (categoryId <= 0) ? -1 : categoryId;
        String finalPriceOrder = (priceOrder == null || priceOrder.trim().isEmpty()) ? ""
                : priceOrder.trim().toUpperCase();
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        int inActiveStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.PRODUCT, Status.Product.INACTIVE).getId();
        PagedResponse<ProductDisplayForImportDTO> pagedData = ProductDAL.getInstance()
                .filterProductsPagedForImport(cleanKeyword, finalCategoryId, finalPriceOrder, inActiveStatusId,
                        finalPageIndex,
                        finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    /**
     * Filter products for selling with pagination
     * Returns ACTIVE products with selling price for order placement
     *
     * @param keyword    Product name keyword (empty = all)
     * @param categoryId Category filter (-1 = all categories)
     * @param priceOrder Sort order: "" (none), "ASC" (low to high), "DESC" (high to
     *                   low)
     * @param pageIndex  Page number (0-indexed)
     * @param pageSize   Items per page
     * @return BUSResult with PagedResponse<ProductDisplayForSellingDTO>
     */
    public BUSResult filterProductsPagedForSelling(String keyword, int categoryId, String priceOrder, int pageIndex,
            int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int finalCategoryId = (categoryId <= 0) ? -1 : categoryId;
        String finalPriceOrder = (priceOrder == null || priceOrder.trim().isEmpty()) ? ""
                : priceOrder.trim().toUpperCase();
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;

        // Get ACTIVE status ID (we want to sell active products only)
        int activeStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.PRODUCT, Status.Product.ACTIVE).getId();

        PagedResponse<ProductDisplayForSellingDTO> pagedData = ProductDAL.getInstance()
                .filterProductsPagedForSelling(cleanKeyword, finalCategoryId, finalPriceOrder, activeStatusId,
                        finalPageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }
}
