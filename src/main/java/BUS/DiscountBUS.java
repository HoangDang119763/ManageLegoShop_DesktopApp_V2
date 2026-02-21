package BUS;

import DAL.ConnectApplication;
import DAL.DiscountDAL;
import DTO.BUSResult;
import DTO.DetailDiscountDTO;
import DTO.DiscountDTO;
import DTO.PagedResponse;
import ENUM.BUSOperationResult;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DiscountBUS extends BaseBUS<DiscountDTO, String> {
    private static final DiscountBUS INSTANCE = new DiscountBUS();

    private DiscountBUS() {
    }

    public static DiscountBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<DiscountDTO> getAll() {
        return DiscountDAL.getInstance().getAll();
    }

    @Override
    protected String getKey(DiscountDTO obj) {
        return obj.getCode();
    }

    public ArrayList<DiscountDTO> filterDiscountsAdvance(String discountName, int type, LocalDate startDate,
            LocalDate endDate) {
        ArrayList<DiscountDTO> filteredList = new ArrayList<>();

        // for (DiscountDTO dis : arrLocal) {
        // boolean matchesDate = true;
        // boolean matchesOther = false;

        // LocalDate discountStartDate = dis.getStartDate().toLocalDate();
        // LocalDate discountEndDate = dis.getEndDate().toLocalDate();

        // // Xử lý logic ngày
        // if (startDate != null && endDate != null) {
        // matchesDate = !discountEndDate.isAfter(endDate);
        // } else if (startDate != null) {
        // matchesDate = !discountStartDate.isBefore(startDate);
        // } else if (endDate != null) {
        // matchesDate = !discountEndDate.isAfter(endDate);
        // }

        // if (discountName != null && !discountName.isBlank()) {
        // if (dis.getName().toLowerCase().contains(discountName.toLowerCase())) {
        // matchesOther = true;
        // }
        // }

        // if (type != -1) {
        // if (dis.getType() == type) {
        // matchesOther = true;
        // }
        // }

        // // Nếu không nhập gì => mặc định true
        // if ((discountName == null || discountName.isBlank()) && type == -1) {
        // matchesOther = true;
        // }

        // if (matchesDate && matchesOther) {
        // filteredList.add(new DiscountDTO(dis));
        // }
        // }

        return filteredList;
    }

    public BUSResult insertFullDiscount(DiscountDTO obj, List<DetailDiscountDTO> details) {
        if (obj == null || details == null || details.isEmpty())
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 1. Chuẩn hóa & Validate cơ bản
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setCode(obj.getCode().toUpperCase().trim());
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        if (!isValidateDiscountInput(obj, false))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        // 2. Validate logic discount amount (mốc giá tăng => amount phải tăng hoặc
        // bằng)
        if (!isValidDetailDiscountAmountLogic(details))
            return new BUSResult(BUSOperationResult.INVALID_DATA,
                    "Số tiền giảm giá không hợp lệ. Mốc giá lớn hơn phải có discount ≥ mốc giá nhỏ hơn.");

        // 3. Kiểm tra trùng Code
        if (DiscountDAL.getInstance().existsByCode(obj.getCode(), null))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.DISCOUNT_ADD_DUPLICATE);

        Connection conn = null;
        BUSResult finalResult = null;

        try {
            conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            conn.setAutoCommit(false);

            // Bước A: Lưu Master
            if (!DiscountDAL.getInstance().insert(conn, obj)) {
                throw new Exception("MASTER_FAIL");
            }

            // Bước B: Lưu Details
            if (!DetailDiscountBUS.getInstance().insertDetailDiscountWithConn(conn, obj.getCode(), obj.getType(),
                    details)) {
                throw new Exception("DETAIL_FAIL");
            }

            conn.commit();
            finalResult = new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DISCOUNT_ADD_SUCCESS);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if ("MASTER_FAIL".equals(e.getMessage()) || "DETAIL_FAIL".equals(e.getMessage())) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, AppMessages.UNKNOWN_ERROR);
            } else {
                finalResult = new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalResult;
    }

    public BUSResult deleteFullDiscount(String code) {
        // 1. Validate đầu vào
        if (code == null || code.isEmpty())
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 2. Kiểm tra ràng buộc dữ liệu (Invoice)
        if (InvoiceBUS.getInstance().isDiscountInAnyInvoice(code)) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.DISCOUNT_IN_USED);
        }

        Connection conn = null;
        BUSResult finalResult = null; // Biến tạm lưu kết quả

        try {
            conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            conn.setAutoCommit(false);

            // Bước A: Xóa chi tiết
            if (!DetailDiscountBUS.getInstance().deleteByDiscountCodeWithConn(conn, code)) {
                // Thay vì throw, ta gán lỗi và tự rollback thủ công hoặc throw custom
                throw new Exception("DETAIL_FAIL");
            }

            // Bước B: Xóa Master
            if (!DiscountDAL.getInstance().delete(conn, code)) {
                throw new Exception("MASTER_FAIL");
            }

            conn.commit();
            finalResult = new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DISCOUNT_DELETE_SUCCESS);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            // Phân loại lỗi để trả về BUSResult đồng bộ
            if ("DETAIL_FAIL".equals(e.getMessage()) || "MASTER_FAIL".equals(e.getMessage())) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, AppMessages.UNKNOWN_ERROR);
            } else {
                finalResult = new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
            }

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalResult;
    }

    public BUSResult updateFullDiscount(DiscountDTO obj, List<DetailDiscountDTO> details) {
        if (obj == null || details == null || details.isEmpty())
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        DiscountDTO oldObj = DiscountDAL.getInstance().getById(obj.getCode());
        if (oldObj == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }

        // Kiểm tra khóa ngày bắt đầu
        if (InvoiceBUS.getInstance().isDiscountInAnyInvoice(obj.getCode())) {
            if (!obj.getStartDate().toLocalDate().equals(oldObj.getStartDate().toLocalDate())) {
                System.err.println(
                        "[DEBUG] Update blocked: Discount already used in invoices. StartDate cannot be changed.");
                return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.DISCOUNT_LOCK_START_DATE);
            }
        }

        // Kiểm tra khuyến mãi đã kết thúc trong quá khứ (không cho sửa KM đã hết hạn)
        if (oldObj.getEndDate().toLocalDate().isBefore(LocalDate.now())) {
            System.err.println("[DEBUG] Update blocked: Cannot edit an expired discount.");
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.DISCOUNT_LOCK_OUTDATE);
        }

        // 1. Chuẩn hóa & Validate
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        if (!isValidateDiscountInput(obj, true)) {
            System.err.println("[DEBUG] Validation failed for DiscountDTO input.");
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 2. Validate logic discount amount (mốc giá tăng => amount phải tăng hoặc
        // bằng)
        if (!isValidDetailDiscountAmountLogic(details))
            return new BUSResult(BUSOperationResult.INVALID_DATA,
                    "Số tiền giảm giá không hợp lệ. Mốc giá lớn hơn phải có discount ≥ mốc giá nhỏ hơn.");

        Connection conn = null;
        BUSResult finalResult = null;

        try {
            System.err.println("[DEBUG] Starting Transaction for Update...");
            conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            conn.setAutoCommit(false);

            // Bước A: Cập nhật Master
            if (!DiscountDAL.getInstance().update(conn, obj)) {
                throw new Exception("MASTER_FAIL");
            }

            // Bước B: Xóa sạch Detail cũ (Sửa logic: Nếu trả về FALSE mới throw)
            if (!DetailDiscountBUS.getInstance().deleteByDiscountCodeWithConn(conn, obj.getCode())) {
                throw new Exception("CLEAR_DETAIL_FAIL");
            }

            // Bước C: Chèn lại Detail mới (Sửa logic: Nếu trả về FALSE mới throw)
            if (!DetailDiscountBUS.getInstance().insertDetailDiscountWithConn(conn, obj.getCode(), obj.getType(),
                    details)) {
                throw new Exception("INSERT_DETAIL_FAIL");
            }

            conn.commit();

            finalResult = new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DISCOUNT_UPDATE_SUCCESS);

        } catch (Exception e) {

            if (conn != null) {
                try {
                    conn.rollback();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (e.getMessage() != null && e.getMessage().contains("FAIL")) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, AppMessages.UNKNOWN_ERROR);
            } else {
                e.printStackTrace(); // In stack trace để xem lỗi SQL cụ thể nếu có
                finalResult = new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalResult;
    }

    /**
     * Validate discount amount logic: Mốc giá tăng => amount phải tăng hoặc bằng
     */
    private boolean isValidDetailDiscountAmountLogic(List<DetailDiscountDTO> details) {
        if (details == null || details.isEmpty()) {
            return false;
        }

        // Sort theo totalPriceInvoice tăng dần
        List<DetailDiscountDTO> sortedList = new ArrayList<>(details);
        sortedList.sort((d1, d2) -> d1.getTotalPriceInvoice().compareTo(d2.getTotalPriceInvoice()));

        // Validate logic: mốc giá tăng => amount phải tăng hoặc bằng
        for (int i = 1; i < sortedList.size(); i++) {
            DetailDiscountDTO prev = sortedList.get(i - 1);
            DetailDiscountDTO curr = sortedList.get(i);

            // Nếu price > price_trước => amount phải >= amount_trước
            if (curr.getTotalPriceInvoice().compareTo(prev.getTotalPriceInvoice()) > 0) {
                if (curr.getDiscountAmount().compareTo(prev.getDiscountAmount()) < 0) {
                    return false; // Mốc lớn hơn có discount nhỏ hơn => INVALID
                }
            }
        }

        return true;
    }

    private boolean isValidateDiscountInput(DiscountDTO obj, boolean isEditMode) {
        // 1. Kiểm tra Null/Empty (Giữ nguyên)
        if (obj == null || obj.getName() == null || obj.getCode() == null ||
                obj.getStartDate() == null || obj.getEndDate() == null) {
            return false;
        }

        ValidationUtils validator = ValidationUtils.getInstance();

        // 2. Validate định dạng chuỗi (Giữ nguyên)
        if (!validator.validateDiscountCode(obj.getCode(), 4, 50) ||
                !validator.validateVietnameseText100(obj.getName())) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate start = obj.getStartDate().toLocalDate();
        LocalDate end = obj.getEndDate().toLocalDate();

        // 3. Logic thời gian chung cho cả Add/Edit
        if (end.isBefore(start))
            return false; // Kết thúc không được trước bắt đầu
        if (end.isBefore(today))
            return false; // Không được chốt ngày kết thúc ở quá khứ

        // 4. Logic riêng cho trường hợp INSERT
        if (!isEditMode) {
            if (start.isBefore(today))
                return false;
        }

        return true;
    }

    @Override
    public DiscountDTO getById(String id) {
        if (id == null || id.isEmpty())
            return null;
        return DiscountDAL.getInstance().getById(id);
    }

    /**
     * [OPTIMIZED] Filter discounts with pagination for manage display
     */
    public BUSResult filterDiscountsPagedForManage(String keyword, int pageIndex, int pageSize) {
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        String searchKeyword = (keyword == null) ? "" : keyword.trim();

        PagedResponse<DiscountDTO> pagedData = DiscountDAL.getInstance()
                .filterDiscountsPagedForManage(searchKeyword, finalPageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

}
