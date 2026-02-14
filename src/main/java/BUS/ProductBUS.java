package BUS;

import DAL.ProductDAL;

import DTO.ProductDTO;
import DTO.BUSResult;
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

    // --- LOGIC XỬ LÝ ID AN TOÀN ---
    public String autoId() {
        if (isLocalEmpty())
            return "SP00001";

        // Quét tìm Max ID thực sự thay vì lấy phần tử cuối (tránh lỗi khi list bị sort)
        int maxId = 0;
        for (ProductDTO p : arrLocal) {
            try {
                int currentNumId = Integer.parseInt(p.getId().substring(2));
                if (currentNumId > maxId)
                    maxId = currentNumId;
            } catch (Exception e) {
                /* Bỏ qua mã lỗi định dạng */ }
        }
        return String.format("SP%05d", maxId + 1);
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
        if (!cateBus.isValidCategory(obj.getCategoryId()))
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
        ProductDTO freshProduct = ProductDAL.getInstance().getById(obj.getId());
        addToLocalCache(freshProduct);
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_ADD_SUCCESS);
    }

    public BUSResult update(ProductDTO obj) {
        if (obj == null || obj.getId() == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        ProductDTO oldProduct = getByIdLocal(obj.getId());
        if (oldProduct == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        int inactiveId = StatusBUS.getInstance()
                .getByTypeAndStatusNameLocal(StatusType.PRODUCT, Status.Product.INACTIVE).getId();

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
        if (isDataUnchanged(obj))
            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_UPDATE_SUCCESS);

        obj.setName(val.normalizeWhiteSpace(obj.getName()));
        obj.setDescription(val.normalizeWhiteSpace(obj.getDescription()));
        obj.setCreatedAt(oldProduct.getCreatedAt()); // Giữ nguyên ngày sinh
        obj.setUpdatedAt(LocalDateTime.now());
        // 4. Lưu DB & Cache
        if (!ProductDAL.getInstance().update(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        ProductDTO freshProduct = ProductDAL.getInstance().getById(obj.getId());
        // 3. Cập nhật Cache bằng đối tượng "Tươi" này
        updateLocalCache(freshProduct);
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_UPDATE_SUCCESS);
    }

    public BUSResult delete(String id) {
        ProductDTO target = getByIdLocal(id);
        if (target == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        int inactiveId = StatusBUS.getInstance()
                .getByTypeAndStatusNameLocal(StatusType.PRODUCT, Status.Product.INACTIVE).getId();

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

        // 5. Cập nhật Cache
        if (hasHistory) {
            ProductDTO freshProduct = ProductDAL.getInstance().getById(id);
            updateLocalCache(freshProduct);
        } else {
            arrLocal.remove(target);
            mapLocal.remove(id); // Giả sử bạn có hàm remove cache theo ID
        }

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
            if (!CategoryBUS.getInstance().isValidCategory(newCateId)) {
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
                .getByTypeAndStatusNameLocal(StatusType.PRODUCT, Status.Product.ACTIVE).getId();

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

            if (!CategoryBUS.getInstance().isValidCategory(p.getCategoryId()))
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

        for (ProductDTO p : listProducts)
            addToLocalCache(p);
        return 1;
    }

    // --- TỐI ƯU HIỆU NĂNG CACHE ---
    public boolean updateQuantitySellingPriceListProduct(ArrayList<ProductDTO> listProducts, boolean isAdd) {
        if (listProducts == null || listProducts.isEmpty())
            return false;

        // Clone và tính toán (Sử dụng Map - O(1))
        for (ProductDTO p : listProducts) {
            ProductDTO local = mapLocal.get(p.getId());
            if (local == null)
                return false;

            int newQty = isAdd ? (local.getStockQuantity() + p.getStockQuantity())
                    : Math.max(0, local.getStockQuantity() - p.getStockQuantity());
            p.setStockQuantity(newQty);
        }

        if (!ProductDAL.getInstance().updateProductQuantityAndSellingPrice(listProducts))
            return false;

        // Cập nhật Cache đồng bộ
        for (ProductDTO updated : listProducts) {
            ProductDTO local = mapLocal.get(updated.getId());
            if (local != null) {
                local.setStockQuantity(updated.getStockQuantity());
                local.setSellingPrice(updated.getSellingPrice());
            }
        }
        return true;
    }

    // --- HÀM HỖ TRỢ & VALIDATE ---
    private boolean isExistProductName(String id, String name) {
        if (name == null)
            return false;
        String normalized = ValidationUtils.getInstance().normalizeWhiteSpace(name);
        for (ProductDTO p : arrLocal) {
            if (!Objects.equals(p.getId(), id) && p.getName().equalsIgnoreCase(normalized))
                return true;
        }
        return false;
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

    public boolean isDataUnchanged(ProductDTO obj) {
        ProductDTO ex = getByIdLocal(obj.getId());
        if (ex == null)
            return false;
        ValidationUtils v = ValidationUtils.getInstance();
        // So sánh toàn bộ field quan trọng để kiểm tra dữ liệu có thay đổi hay không
        return Objects.equals(ex.getName(), v.normalizeWhiteSpace(obj.getName())) &&
                Objects.equals(ex.getDescription(), v.normalizeWhiteSpace(obj.getDescription())) &&
                Objects.equals(ex.getCategoryId(), obj.getCategoryId()) &&
                obj.getSellingPrice().compareTo(ex.getSellingPrice()) == 0 &&
                Objects.equals(ex.getStatusId(), obj.getStatusId()) &&
                Objects.equals(ex.getImageUrl(), obj.getImageUrl());
    }

    private void addToLocalCache(ProductDTO obj) {
        ProductDTO copy = new ProductDTO(obj);
        mapLocal.put(copy.getId(), copy);
        arrLocal.add(copy);
    }

    private void updateLocalCache(ProductDTO product) {
        // 1. Cập nhật Map (Ghi đè reference mới)
        mapLocal.put(product.getId(), product);

        // 2. Cập nhật List (Tìm và thay thế)
        for (int i = 0; i < arrLocal.size(); i++) {
            if (arrLocal.get(i).getId().equals(product.getId())) {
                arrLocal.set(i, product);
                break;
            }
        }
    }

    public boolean isDuplicateImageUrl(ProductDTO obj) {
        ProductDTO existingPro = getByIdLocal(obj.getId());
        return existingPro != null &&
                Objects.equals(existingPro.getImageUrl(), obj.getImageUrl());
    }

    public ArrayList<ProductDTO> filterProducts(String searchBy, String keyword, int categoryIdFilter, int statusFilter,
            BigDecimal startPrice, BigDecimal endPrice, boolean inStockOnly) {
        ArrayList<ProductDTO> filteredList = new ArrayList<>();

        if (keyword == null)
            keyword = "";
        if (searchBy == null)
            searchBy = "";

        keyword = keyword.trim().toLowerCase();

        for (ProductDTO pro : arrLocal) {
            boolean matchesSearch = true;
            boolean matchesCategory = (categoryIdFilter == -1) || (pro.getCategoryId() == categoryIdFilter);
            boolean matchesStatus = (statusFilter == -1) || (pro.getStatusId() == statusFilter);
            boolean matchesPrice = true;
            boolean matchesStock = !inStockOnly || pro.getStockQuantity() > 0;

            // Giá
            if (startPrice != null && endPrice != null) {
                matchesPrice = pro.getSellingPrice().compareTo(startPrice) >= 0 &&
                        pro.getSellingPrice().compareTo(endPrice) <= 0;
            } else if (startPrice != null) {
                matchesPrice = pro.getSellingPrice().compareTo(startPrice) >= 0;
            } else if (endPrice != null) {
                matchesPrice = pro.getSellingPrice().compareTo(endPrice) <= 0;
            }

            String name = pro.getName() != null ? pro.getName().toLowerCase() : "";
            String productId = pro.getId() != null ? pro.getId().toLowerCase() : "";

            if (!keyword.isEmpty()) {
                switch (searchBy) {
                    case "Mã sản phẩm" -> matchesSearch = productId.contains(keyword);
                    case "Tên sản phẩm" -> matchesSearch = name.contains(keyword);
                }
            }

            if (matchesSearch && matchesCategory && matchesStatus && matchesPrice && matchesStock) {
                filteredList.add(pro);
            }
        }

        return filteredList;
    }

    public ArrayList<ProductDTO> filterProducts(String searchBy, String keyword, int categoryIdFilter,
            int statusFilter, BigDecimal startPrice, BigDecimal endPrice) {
        return filterProducts(searchBy, keyword, categoryIdFilter, statusFilter, startPrice, endPrice, false);
    }

    public boolean isCategoryInAnyProduct(int categoryId) {
        // 1. Đảm bảo dữ liệu đã được load (Giữ nguyên logic của bạn)
        if (this.isLocalEmpty())
            this.loadLocal();
        for (ProductDTO p : arrLocal) {
            if (p.getCategoryId() == categoryId) {
                return true;
            }
        }
        return false;
    }
}