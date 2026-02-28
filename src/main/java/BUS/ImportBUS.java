package BUS;

import DAL.ConnectApplication;
import DAL.DetailImportDAL;
import DAL.ImportDAL;
import DAL.ProductDAL;
import DTO.BUSResult;
import DTO.DetailImportDTO;
import DTO.DetailPushedInfoDTO;
import DTO.ImportDTO;
import DTO.ImportDisplayDTO;
import DTO.PagedResponse;
import DTO.PriceUpdateInfoDTO;
import DTO.UpdateStockInfoDTO;
import ENUM.BUSOperationResult;
import ENUM.StatusType;
import ENUM.Status;
import UTILS.AppMessages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportBUS extends BaseBUS<ImportDTO, Integer> {
    private static final ImportBUS INSTANCE = new ImportBUS();

    public static ImportBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<ImportDTO> getAll() {
        return ImportDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(ImportDTO obj) {
        return obj.getId();
    }

    public int nextId() {
        return ImportDAL.getInstance().getLastIdEver() + 1;
    }

    public boolean isProductInAnyImport(String productId) {
        DetailImportBUS detailImportBUS = DetailImportBUS.getInstance();

        // 2. Sử dụng Stream để tìm kiếm nhanh và hiện đại
        // Kiểm tra xem có bất kỳ dòng chi tiết nhập hàng nào chứa mã sản phẩm này không
        return detailImportBUS.getAll().stream()
                .anyMatch(detail -> detail.getProductId().equals(productId));
    }

    @Override
    public ImportDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return ImportDAL.getInstance().getById(id);
    }

    public boolean isSupplierInAnyImport(int supplierId) {
        if (supplierId <= 0)
            return false;
        return ImportDAL.getInstance().existsBySupplierId(supplierId);
    }

    public boolean isEmployeeInAnyImport(int employeeId) {
        if (employeeId <= 0)
            return false;
        return ImportDAL.getInstance().existsByEmployeeId(employeeId);
    }

    /**
     * [OPTIMIZED] Filter imports with pagination for manage display
     */
    public BUSResult filterImportsPagedForManage(String keyword, int statusId, int pageIndex, int pageSize) {
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? 10 : pageSize;
        int finalSearchId = (keyword == null || keyword.trim().isEmpty()) ? -1 : Integer.parseInt(keyword);
        int finalStatusId = (statusId <= 0) ? -1 : statusId;

        // Gọi DAL với JOIN để lấy dữ liệu hoàn chỉnh
        PagedResponse<ImportDisplayDTO> pagedData = ImportDAL.getInstance()
                .filterImportsPagedForManage(finalSearchId, finalStatusId, finalPageIndex, finalPageSize);
        if (pagedData == null) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.UNKNOWN_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    /**
     * Insert Import with Detail Imports using transaction (Optimized Batch Version)
     * Tạo phiếu nhập với các chi tiết phiếu nhập sử dụng transaction - trạng thái
     * ban đầu là DRAFT
     *
     * @param importDTO        the import record
     * @param detailImportList the list of detail import records
     * @return BUSResult with status and message
     */
    public BUSResult insertFullImport(ImportDTO importDTO, List<DetailImportDTO> detailImportList) {
        // 1. Validate parameters
        if (importDTO == null || detailImportList == null || detailImportList.isEmpty())
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        Connection conn = null;
        BUSResult finalResult = null;
        String finalMessage = "";

        try {
            // Get connection from pool
            conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            conn.setAutoCommit(false);

            // Set status to DRAFT - tạo bản nháp
            int draftStatusId = StatusBUS.getInstance()
                    .getByTypeAndStatusName(StatusType.IMPORT, Status.Import.DRAFT).getId();
            importDTO.setStatusId(draftStatusId);

            // Step A: Insert Import master record
            if (!ImportDAL.getInstance().insert(conn, importDTO)) {
                throw new Exception("IMPORT_MASTER_FAIL");
            }

            // Step B: Insert Detail Import records
            if (!DetailImportDAL.getInstance().insertAllDetailImportByImportId(conn, importDTO.getId(),
                    new ArrayList<>(detailImportList))) {
                throw new Exception("DETAIL_IMPORT_FAIL");
            }

            // Commit transaction
            conn.commit();

            finalMessage = "Tạo phiếu nhập thành công! (Trạng thái: DRAFT - Chưa duyệt)";
            finalResult = new BUSResult(BUSOperationResult.SUCCESS, finalMessage);

        } catch (Exception e) {
            // Rollback on any error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            // Return appropriate error message
            String errCode = e.getMessage();
            if ("IMPORT_MASTER_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi tạo phiếu nhập.");
            } else if ("DETAIL_IMPORT_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi tạo chi tiết phiếu nhập.");
            } else {
                finalResult = new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR + ": " + errCode);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalResult;
    }

    /**
     * Approve an import in DRAFT status - thực hiện duyệt phiếu nhập
     * Cập nhật tồn kho, giá bán sản phẩm theo quy tắc
     *
     * @param importId the import ID to approve
     * @return BUSResult with status and message
     */
    public BUSResult approveImport(Integer importId) {
        if (importId == null || importId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        Connection conn = null;
        BUSResult finalResult = null;
        String finalMessage = "";

        try {
            // Get connection from pool
            conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            conn.setAutoCommit(false);

            // Get import and verify status is DRAFT
            ImportDTO importDTO = ImportDAL.getInstance().getById(importId);
            if (importDTO == null) {
                return new BUSResult(BUSOperationResult.FAIL, "Không tìm thấy phiếu nhập.");
            }

            int draftStatusId = StatusBUS.getInstance()
                    .getByTypeAndStatusName(StatusType.IMPORT, Status.Import.DRAFT).getId();

            if (importDTO.getStatusId() != draftStatusId) {
                return new BUSResult(BUSOperationResult.FAIL, "Chỉ có thể duyệt các phiếu nhập ở trạng thái DRAFT.");
            }

            // Get detail import list
            ArrayList<DetailImportDTO> detailImportList = DetailImportDAL.getInstance()
                    .getAllDetailImportByImportId(conn, importId);

            if (detailImportList == null || detailImportList.isEmpty()) {
                return new BUSResult(BUSOperationResult.FAIL, "Phiếu nhập không có chi tiết sản phẩm.");
            }

            int inCompletedStatusId = StatusBUS.getInstance()
                    .getByTypeAndStatusName(StatusType.IMPORT, Status.Import.INCOMPLETED).getId();
            int completedStatusId = StatusBUS.getInstance()
                    .getByTypeAndStatusName(StatusType.IMPORT, Status.Import.COMPLETED).getId();

            // Step C: Batch lấy tồn kho của tất cả sản phẩm (1 query)
            List<String> productIds = detailImportList.stream()
                    .map(DetailImportDTO::getProductId)
                    .collect(Collectors.toList());
            Map<String, Integer> stockMap = ProductDAL.getInstance().getStocksByProductIds(conn, productIds);

            // Step D: Duyệt danh sách detail 1 lần để tạo 3 danh sách batch
            List<UpdateStockInfoDTO> stockUpdateList = new ArrayList<>();
            List<PriceUpdateInfoDTO> priceUpdateList = new ArrayList<>();
            List<DetailPushedInfoDTO> pushedUpdateList = new ArrayList<>();
            int pushedCount = 0;

            for (DetailImportDTO detail : detailImportList) {
                int currentStock = stockMap.getOrDefault(detail.getProductId(), 0);

                // Chỉ cập nhật stock và giá nếu stock = 0
                if (currentStock == 0) {
                    // Cập nhật tồn kho
                    stockUpdateList.add(new UpdateStockInfoDTO(detail.getProductId(), detail.getQuantity()));
                    // Cập nhật giá: sellingPrice = importPrice * (1 + profitPercent / 100)
                    java.math.BigDecimal importPrice = detail.getPrice();
                    java.math.BigDecimal sellingPrice = importPrice.multiply(
                            java.math.BigDecimal.ONE.add(
                                    detail.getProfitPercent().divide(BigDecimal.valueOf(100), 2,
                                            RoundingMode.HALF_UP)));
                    priceUpdateList.add(new PriceUpdateInfoDTO(detail.getProductId(), importPrice, sellingPrice));
                    // Đánh dấu là đã đẩy giá
                    pushedUpdateList.add(new DetailPushedInfoDTO(detail.getProductId(), true));
                    pushedCount++;
                } else {
                    // Stock > 0, không update stock và giá, đánh dấu là chưa đẩy
                    pushedUpdateList.add(new DetailPushedInfoDTO(detail.getProductId(), false));
                }
            }

            // Step E: Batch update stock (1 query)
            if (!ProductDAL.getInstance().batchUpdateStock(conn, stockUpdateList)) {
                throw new Exception("UPDATE_STOCK_FAIL");
            }

            // Step F: Batch update prices nếu có (1 query)
            if (!priceUpdateList.isEmpty()) {
                if (!ProductDAL.getInstance().batchUpdatePriceAfterImport(conn, priceUpdateList)) {
                    throw new Exception("UPDATE_PRICE_FAIL");
                }
            }

            // Step G: Batch update is_pushed (1 query)
            if (!DetailImportDAL.getInstance().batchUpdateIsPushed(conn, importId, pushedUpdateList)) {
                throw new Exception("UPDATE_PUSHED_FAIL");
            }

            // Step H: Update status - check xem toàn bộ được push hay không
            boolean isAllPushed = pushedCount == detailImportList.size();
            int newStatusId = isAllPushed ? completedStatusId : inCompletedStatusId;

            if (!ImportDAL.getInstance().updateStatus(conn, importId, newStatusId)) {
                throw new Exception("UPDATE_STATUS_FAIL");
            }

            // Commit transaction
            conn.commit();

            // Xây dựng Final Message
            finalMessage = String.format("Duyệt phiếu nhập thành công! (Đã đẩy giá %d/%d sản phẩm).", pushedCount,
                    detailImportList.size());
            if (!isAllPushed) {
                finalMessage += "\nLưu ý: Một số sản phẩm vẫn giữ giá cũ do còn tồn kho.";
            }

            finalResult = new BUSResult(BUSOperationResult.SUCCESS, finalMessage);

        } catch (Exception e) {
            // Rollback on any error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            // Return appropriate error message
            String errCode = e.getMessage();
            if ("UPDATE_STOCK_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi cập nhật tồn kho sản phẩm.");
            } else if ("UPDATE_PRICE_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi cập nhật giá bán mới.");
            } else if ("UPDATE_PUSHED_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi cập nhật trạng thái pushed.");
            } else if ("UPDATE_STATUS_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi cập nhật trạng thái phiếu nhập.");
            } else {
                finalResult = new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR + ": " + errCode);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalResult;
    }

    /**
     * Delete an import in DRAFT status - xóa phiếu nhập (chỉ xóa được khi trạng
     * thái DRAFT)
     *
     * @param importId the import ID to delete
     * @return BUSResult with status and message
     */
    public BUSResult deleteImport(Integer importId) {
        if (importId == null || importId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        Connection conn = null;
        BUSResult finalResult = null;

        try {
            // Get connection from pool
            conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            conn.setAutoCommit(false);

            // Get import and verify status is DRAFT
            ImportDTO importDTO = ImportDAL.getInstance().getById(importId);
            if (importDTO == null) {
                return new BUSResult(BUSOperationResult.FAIL, "Không tìm thấy phiếu nhập.");
            }

            int draftStatusId = StatusBUS.getInstance()
                    .getByTypeAndStatusName(StatusType.IMPORT, Status.Import.DRAFT).getId();

            if (importDTO.getStatusId() != draftStatusId) {
                return new BUSResult(BUSOperationResult.FAIL, "Chỉ có thể xóa các phiếu nhập ở trạng thái DRAFT.");
            }

            // Delete detail imports first
            if (!DetailImportDAL.getInstance().deleteByImportId(conn, importId)) {
                throw new Exception("DELETE_DETAIL_FAIL");
            }

            // Delete master import
            if (!ImportDAL.getInstance().delete(conn, importId)) {
                throw new Exception("DELETE_MASTER_FAIL");
            }

            // Commit transaction
            conn.commit();

            finalResult = new BUSResult(BUSOperationResult.SUCCESS, "Xóa phiếu nhập thành công!");

        } catch (Exception e) {
            // Rollback on any error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            // Return appropriate error message
            String errCode = e.getMessage();
            if ("DELETE_DETAIL_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi xóa chi tiết phiếu nhập.");
            } else if ("DELETE_MASTER_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi xóa phiếu nhập.");
            } else {
                finalResult = new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR + ": " + errCode);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalResult;
    }
}
