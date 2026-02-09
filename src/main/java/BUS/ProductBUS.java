package BUS;

import DAL.ProductDAL;

import DTO.ProductDTO;
import DTO.BUSResult;
import DTO.EmployeeDTO;
import ENUM.*;
import SERVICE.AuthorizationService;
import UTILS.AppMessages;
import UTILS.AvailableUtils;
import UTILS.ValidationUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
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

    private void updateLocalCache(ProductDTO obj) {
        // Tạo bản sao mới để tránh side-effect từ UI
        ProductDTO clonedObj = new ProductDTO(obj);

        // Cập nhật Map (Nhanh - O(1))
        mapLocal.put(clonedObj.getId(), clonedObj);

        // Cập nhật List (Dùng để hiển thị lên TableView)
        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), clonedObj.getId())) {
                // Thay thế bằng đối tượng đã clone
                arrLocal.set(i, clonedObj);
                break;
            }
        }
    }

    public BUSResult delete(String id) {
        // 1. Kiểm tra đầu vào & kho (Chuẩn)
        if (id == null || id.isEmpty())
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        ProductDTO targetProduct = getByIdLocal(id);
        if (targetProduct == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        if (targetProduct.getStockQuantity() != 0)
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.PRODUCT_DELETE_WITH_STOCK);

        // 2. Quyết định xóa (Chuẩn)
        boolean isInInvoice = InvoiceBUS.getInstance().isProductInCompletedInvoice(id);
        boolean success = isInInvoice ? ProductDAL.getInstance().softDelete(targetProduct)
                : ProductDAL.getInstance().delete(targetProduct.getId());

        if (!success)
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        // 3. Đồng bộ Cache
        if (isInInvoice) {
            // Lấy ID từ Cache của StatusBUS (nhanh, không sợ lag)
            int inactiveStatusId = StatusBUS.getInstance()
                    .getByTypeAndStatusNameLocal(StatusType.PRODUCT, Status.Product.INACTIVE).getId();

            targetProduct.setStatusId(inactiveStatusId);
            mapLocal.put(id, targetProduct);
        } else {
            // Xóa cứng: Bay màu hoàn toàn khỏi cả 2 danh sách
            arrLocal.remove(targetProduct);
            mapLocal.remove(id);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_DELETE_SUCCESS);
    }

    public String autoId() {
        if (isLocalEmpty()) {
            return "SP00001";
        }

        String lastId = arrLocal.get(arrLocal.size() - 1).getId();
        try {
            int id = Integer.parseInt(lastId.substring(2)) + 1;
            return String.format("SP%05d", id);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid product ID format: " + lastId);
        }
    }

    private String nextProductId(String currentId) {
        int temp = Integer.parseInt(currentId.substring(2)) + 1;
        return String.format("SP%05d", temp);
    }

    public BUSResult insert(ProductDTO obj) {
        if (obj == null || obj.getCategoryId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }
        if (!isValidProductInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }
        // 1. Chuẩn hóa ngay từ đầu để Validate cho chính xác
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        obj.setDescription(validate.normalizeWhiteSpace(obj.getDescription()));

        // 2. Check logic (Lúc này tên đã sạch sẽ)
        if (!CategoryBUS.getInstance().isValidCategory(obj.getCategoryId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.PRODUCT_CATEGORY_INVALID);

        if (isDuplicateProductName("", obj.getName())) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.PRODUCT_ADD_DUPLICATE);
        }

        // 3. Thiết lập thông số mặc định cho hàng mới
        obj.setId(autoId());
        obj.setStockQuantity(0);
        obj.setSellingPrice(BigDecimal.ZERO);
        obj.setImportPrice(BigDecimal.ZERO); // Nên set luôn cả giá nhập mặc định

        // 4. Ghi DB
        if (!ProductDAL.getInstance().insert(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        // 5. Cập nhật Cache
        addToLocalCache(obj);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_ADD_SUCCESS);
    }

    private void addToLocalCache(ProductDTO obj) {
        // Deep copy để an toàn tham chiếu
        ProductDTO newProd = new ProductDTO(obj);

        // Thêm mới hoàn toàn vào Map và List
        mapLocal.put(newProd.getId(), newProd);
        arrLocal.add(newProd);

        // Nếu dùng JavaFX TableView, nó sẽ tự động hiện dòng mới ngay lập tức tại đây
    }

    public int insertListProductExcel(ArrayList<ProductDTO> listProducts) {
        if (listProducts == null || listProducts.isEmpty()) {
            return 2; // Danh sách rỗng
        }

        CategoryBUS cate = CategoryBUS.getInstance();
        Set<String> excelProductNames = new HashSet<>();
        ValidationUtils validate = ValidationUtils.getInstance();
        String id = autoId();

        // Duyệt qua từng sản phẩm trong danh sách
        for (ProductDTO p : listProducts) {
            if (!isValidProductInputForExcel(p))
                return 3; // Kiểm tra tính hợp lệ của sản phẩm

            // Kiểm tra tên trùng trong danh sách Excel
            String normalizedName = validate.normalizeWhiteSpace(p.getName());
            if (excelProductNames.contains(normalizedName)) {
                p.setName(p.getName() + " (" + id + ")"); // Nếu trùng, thêm ID vào tên
            }
            excelProductNames.add(normalizedName); // Thêm vào danh sách đã kiểm tra tên

            // Kiểm tra danh mục hợp lệ
            if (!cate.isValidCategory(p.getCategoryId()))
                return 4;

            // Kiểm tra trùng tên trong hệ thống
            if (isDuplicateProductName("", p.getName())) {
                p.setName(p.getName() + " (" + id + ")"); // Thêm ID vào tên sản phẩm nếu trùng
            }

            // Thiết lập các giá trị còn lại cho sản phẩm
            p.setId(id); // Tạo ID mới
            p.setDescription(validate.normalizeWhiteSpace(p.getDescription()));
            p.setStockQuantity(0);
            p.setSellingPrice(new BigDecimal(0));
            id = nextProductId(id); // Tạo ID mới cho sản phẩm tiếp theo
        }

        // Lưu sản phẩm vào cơ sở dữ liệu
        if (!ProductDAL.getInstance().insertListProductExcel(listProducts))
            return 7;

        // Thêm vào danh sách cục bộ (arrLocal)
        arrLocal.addAll(new ArrayList<>(listProducts));

        return 1; // Thành công
    }

    // Chỉ sửa name, selling price, description, imageUrl, categoryId, statusId
    public BUSResult update(ProductDTO obj) {
        // 1. Validate cơ bản
        if (obj == null || obj.getId().isEmpty() || obj.getCategoryId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        // 2. Chuẩn hóa dữ liệu TRƯỚC khi check trùng
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        obj.setDescription(validate.normalizeWhiteSpace(obj.getDescription()));

        // 3. Kiểm tra logic (Tên, dữ liệu hợp lệ...)
        if (!isValidProductUpdate(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        if (isDuplicateProductName(obj.getId(), obj.getName())) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.PRODUCT_UPDATE_DUPLICATE);
        }
        if (isDuplicateProduct(obj))
            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_UPDATE_SUCCESS);
        // 5. Ghi xuống Database
        if (!ProductDAL.getInstance().update(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        // 6. Cập nhật vào Cache (Truyền thẳng obj vào, hàm cache sẽ tự clone)
        updateLocalCache(obj);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.PRODUCT_UPDATE_SUCCESS);
    }

    public boolean updateQuantitySellingPriceListProduct(ArrayList<ProductDTO> listProducts, boolean isAdd) {
        if (listProducts == null || listProducts.isEmpty()) {
            return false;
        }

        ArrayList<ProductDTO> tempList = (ArrayList<ProductDTO>) listProducts.clone();

        if (isAdd) {
            for (ProductDTO p : tempList) {
                ProductDTO current = getByIdLocal(p.getId());
                if (current == null)
                    continue;

                p.setStockQuantity(p.getStockQuantity() + current.getStockQuantity());
            }
        } else {
            for (ProductDTO p : tempList) {
                ProductDTO current = getByIdLocal(p.getId());
                if (current == null)
                    return false;

                int newQty = current.getStockQuantity() - p.getStockQuantity();
                p.setStockQuantity(Math.max(0, newQty));
                p.setSellingPrice(p.getSellingPrice()); // giữ nguyên giá
            }
        }

        // Cập nhật database
        if (!ProductDAL.getInstance().updateProductQuantityAndSellingPrice(tempList))
            return false;

        // Cập nhật lại arrLocal
        for (ProductDTO updated : tempList) {
            for (int i = 0; i < arrLocal.size(); i++) {
                if (Objects.equals(arrLocal.get(i).getId(), updated.getId())) {
                    ProductDTO local = arrLocal.get(i);
                    ProductDTO newProduct = new ProductDTO(local); // Giữ nguyên các field khác

                    newProduct.setStockQuantity(updated.getStockQuantity());
                    newProduct.setSellingPrice(updated.getSellingPrice());

                    arrLocal.set(i, newProduct);
                    break;
                }
            }
        }

        return true;
    }

    // VALIDATE IS HERE!!!
    private boolean isDuplicateProductName(String id, String name) {
        if (name == null)
            return false;
        ValidationUtils validate = ValidationUtils.getInstance();
        name = validate.normalizeWhiteSpace(name);
        for (ProductDTO product : arrLocal) {
            if (!Objects.equals(product.getId(), id) && product.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidProductInput(ProductDTO obj) {
        if (obj.getName() == null)
            return false;

        obj.setDescription(
                obj.getDescription() != null && obj.getDescription().trim().isEmpty() ? null : obj.getDescription());
        obj.setImageUrl(obj.getImageUrl() != null && obj.getImageUrl().trim().isEmpty() ? null : obj.getImageUrl());
        ValidationUtils validator = ValidationUtils.getInstance();
        // Kiểm tra mô tả nếu có
        if (obj.getDescription() != null && !validator.validateVietnameseText65k4(obj.getDescription())) {
            return true;
        }
        if (obj.getImageUrl() != null && !validator.validateVietnameseText255(obj.getImageUrl())) {
            return true;
        }

        return validator.validateVietnameseText255(obj.getName());
    }

    public boolean isValidProductInputForExcel(ProductDTO obj) {
        if (obj.getName() == null)
            return false;

        obj.setDescription(
                obj.getDescription() != null && obj.getDescription().trim().isEmpty() ? null : obj.getDescription());
        obj.setImageUrl(obj.getImageUrl() != null && obj.getImageUrl().trim().isEmpty() ? null : obj.getImageUrl());
        ValidationUtils validator = ValidationUtils.getInstance();
        // Kiểm tra mô tả nếu có
        if (obj.getDescription() != null && !validator.validateVietnameseText65k4(obj.getDescription())) {
            return true;
        }
        if (obj.getImageUrl() != null && !validator.validateVietnameseText255(obj.getImageUrl())) {
            return true;
        }

        return validator.validateVietnameseText248(obj.getName());
    }

    private boolean isValidProductUpdate(ProductDTO obj) {
        if (obj == null || obj.getName() == null || obj.getSellingPrice() == null) {
            // System.out.println("1");
            return false;
        }

        // Xử lý mô tả và ảnh trống
        obj.setDescription(
                obj.getDescription() != null && obj.getDescription().trim().isEmpty() ? null : obj.getDescription());
        obj.setImageUrl(obj.getImageUrl() != null && obj.getImageUrl().trim().isEmpty() ? null : obj.getImageUrl());

        ValidationUtils validator = ValidationUtils.getInstance();

        // Kiểm tra mô tả nếu có
        if (obj.getDescription() != null && !validator.validateVietnameseText65k4(obj.getDescription())) {
            return false; // Nếu mô tả không hợp lệ, trả về false
        }

        // Kiểm tra ảnh URL nếu có
        if (obj.getImageUrl() != null && !validator.validateVietnameseText255(obj.getImageUrl())) {
            return false; // Nếu ảnh không hợp lệ, trả về false
        }

        // Kiểm tra số lượng tồn kho không âm, tên và giá bán hợp lệ
        return obj.getStockQuantity() >= 0
                && validator.validateVietnameseText255(obj.getName()) // Kiểm tra tên
                && validator.validateBigDecimal(obj.getSellingPrice(), 10, 2, false); // Kiểm tra giá bán
    }

    public boolean isDuplicateProduct(ProductDTO obj) {
        ProductDTO existingPro = getByIdLocal(obj.getId());
        ValidationUtils validate = ValidationUtils.getInstance();

        // Kiểm tra xem tên, mô tả, có trùng không (không check stockQuantity vì nó thay
        // đổi độc lập)
        return existingPro != null &&
                Objects.equals(existingPro.getName(), validate.normalizeWhiteSpace(obj.getName())) &&
                Objects.equals(existingPro.getCategoryId(), obj.getCategoryId()) &&
                Objects.equals(existingPro.getSellingPrice(), obj.getSellingPrice()) &&
                Objects.equals(existingPro.getStatusId(), obj.getStatusId()) &&
                Objects.equals(existingPro.getDescription(), validate.normalizeWhiteSpace(obj.getDescription())) &&
                Objects.equals(existingPro.getImageUrl(), obj.getImageUrl());
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

}