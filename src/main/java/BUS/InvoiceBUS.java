package BUS;

import DAL.ConnectApplication;
import DAL.DetailImportDAL;
import DAL.DetailInvoiceDAL;
import DAL.ImportDAL;
import DAL.InvoiceDAL;
import DAL.ProductDAL;
import DTO.BUSResult;
import DTO.DetailImportDTO;
import DTO.DetailInvoiceDTO;
import DTO.DetailPushedInfoDTO;
import DTO.InvoiceDTO;
import DTO.InvoiceDisplayDTO;
import DTO.InvoicePDFDTO;
import DTO.PagedResponse;
import DTO.PriceUpdateInfoDTO;
import DTO.ProductStockPriceInfoDTO;
import DTO.UpdateStockInfoDTO;
import DTO.UpdateStockWithCostDTO;
import ENUM.*;
import UTILS.AppMessages;
import UTILS.ValidationUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Kiểm tra xem sản phẩm có trong hóa đơn có trạng thái COMPLETED hay không
     *
     * @param productId ID của sản phẩm
     * @return true nếu sản phẩm có trong invoice hoàn thành
     */
    public boolean isProductInAnyInvoice(String productId) {
        DetailInvoiceBUS detailInvoiceBUS = DetailInvoiceBUS.getInstance();

        // 2. Sử dụng Stream để tìm kiếm nhanh và hiện đại
        // Kiểm tra xem có bất kỳ dòng chi tiết hóa đơn nào chứa mã sản phẩm này không
        return detailInvoiceBUS.getAll().stream()
                .anyMatch(detail -> detail.getProductId().equals(productId));
    }

    public boolean isCustomerInAnyInvoice(int customerId) {
        if (customerId <= 0)
            return false;
        return InvoiceDAL.getInstance().existsByCustomerId(customerId);
    }

    public boolean isEmployeeInAnyInvoice(int employeeId) {
        if (employeeId <= 0)
            return false;
        return InvoiceDAL.getInstance().existsByEmployeeId(employeeId);
    }

    public boolean isDiscountInAnyInvoice(String discountCode) {
        if (discountCode == null || discountCode.isEmpty())
            return false;
        return InvoiceDAL.getInstance().existsByDiscountCode(discountCode);
    }

    @Override
    public InvoiceDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return InvoiceDAL.getInstance().getById(id);
    }

    public int nextId() {
        return InvoiceDAL.getInstance().getLastIdEver() + 1;
    }

    /**
     * [OPTIMIZED] Get all invoices with pagination for manage display
     * Tránh gọi BUS lẻ lẻ từng dòng
     */
    public BUSResult getAllInvoicesPagedForManage(int pageIndex, int pageSize) {
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;

        // Gọi DAL với JOIN để lấy dữ liệu hoàn chỉnh
        PagedResponse<InvoiceDisplayDTO> pagedData = InvoiceDAL.getInstance()
                .getAllInvoicesPagedForManage(finalPageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    /**
     * [OPTIMIZED] Filter invoices with pagination for manage display
     */
    public BUSResult filterInvoicesPagedForManage(String keyword, int statusId, int pageIndex, int pageSize) {
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        int finalStatusId = (statusId <= 0) ? -1 : statusId;

        int finalSearchId = -1;
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                finalSearchId = Integer.parseInt(keyword.trim());
            }
        } catch (NumberFormatException e) {
            finalSearchId = -1;
        }

        // Gọi DAL với JOIN để lấy dữ liệu hoàn chỉnh
        PagedResponse<InvoiceDisplayDTO> pagedData = InvoiceDAL.getInstance()
                .filterInvoicesPagedForManage(finalSearchId, finalStatusId, finalPageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    /**
     * Insert Invoice with Detail Invoices using transaction (Optimized Batch
     * Version)
     * Tạo phiếu bán với các chi tiết phiếu bán sử dụng transaction - tối ưu batch
     *
     * @param invoiceDTO        the invoice record
     * @param detailInvoiceList the list of detail invoice records
     * @return BUSResult with status and message
     */
    public BUSResult insertFullInvoice(InvoiceDTO invoiceDTO, List<DetailInvoiceDTO> detailInvoiceList) {
        // Step A: Validate parameters
        if (invoiceDTO == null || detailInvoiceList == null || detailInvoiceList.isEmpty())
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        Connection conn = null;
        BUSResult finalResult = null;
        String finalMessage = "";

        try {
            // Get connection from pool
            conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            conn.setAutoCommit(false);

            int completedStatusId = StatusBUS.getInstance()
                    .getByTypeAndStatusName(StatusType.INVOICE, Status.Invoice.COMPLETED).getId();
            invoiceDTO.setStatusId(completedStatusId);

            // Step B: Insert Invoice master record
            if (!InvoiceDAL.getInstance().insert(conn, invoiceDTO)) {
                throw new Exception("INVOICE_MASTER_FAIL");
            }

            // Step C: Batch get current stock AND import price for all products (1 query)
            List<String> productIds = detailInvoiceList.stream()
                    .map(DetailInvoiceDTO::getProductId)
                    .collect(Collectors.toList());
            Map<String, ProductStockPriceInfoDTO> stockPriceMap = ProductDAL.getInstance()
                    .getStockAndPriceByProductIds(conn, productIds);

            // Step D: Loop through details once to validate & assign cost price BEFORE
            // insert
            List<UpdateStockWithCostDTO> stockUpdateWithCostList = new ArrayList<>();
            List<PriceUpdateInfoDTO> priceUpdateList = new ArrayList<>();
            List<DetailPushedInfoDTO> pushedDetailsList = new ArrayList<>();
            List<String> productsZeroStock = new ArrayList<>();

            for (DetailInvoiceDTO detail : detailInvoiceList) {
                ProductStockPriceInfoDTO stockPriceInfo = stockPriceMap.getOrDefault(detail.getProductId(), null);
                int currentStock = (stockPriceInfo != null) ? stockPriceInfo.getStockQuantity() : 0;
                BigDecimal currentCostPrice = (stockPriceInfo != null) ? stockPriceInfo.getImportPrice()
                        : BigDecimal.ZERO;

                // Validate: quantity <= currentStock
                if (detail.getQuantity() > currentStock) {
                    throw new Exception("INSUFFICIENT_STOCK");
                }

                // ISSUE #2 FIX: Assign cost price snapshot to detail BEFORE inserting
                detail.setCostPrice(currentCostPrice);

                // Add to stock update list with cost price snapshot (subtract quantity)
                stockUpdateWithCostList.add(
                        new UpdateStockWithCostDTO(detail.getProductId(), -detail.getQuantity(), currentCostPrice));

                // Check if stock becomes 0 after selling
                int newStock = currentStock - detail.getQuantity();
                if (newStock == 0) {
                    productsZeroStock.add(detail.getProductId());
                }
            }

            // Step E: NOW insert detail invoices with cost price populated
            if (!DetailInvoiceDAL.getInstance().insertAllDetailInvoiceByInvoiceId(conn, invoiceDTO.getId(),
                    new ArrayList<>(detailInvoiceList))) {
                throw new Exception("DETAIL_INVOICE_FAIL");
            }

            // Step F: Batch update stock (subtract quantities) (1 query)
            List<UpdateStockInfoDTO> stockUpdateList = new ArrayList<>();
            for (UpdateStockWithCostDTO costUpdate : stockUpdateWithCostList) {
                stockUpdateList.add(new UpdateStockInfoDTO(costUpdate.getProductId(), costUpdate.getQuantity()));
            }

            if (!ProductDAL.getInstance().batchUpdateStock(conn, stockUpdateList)) {
                throw new Exception("UPDATE_STOCK_FAIL");
            }

            // Step G.1: [OPTIMIZED] For products that became 0 stock, batch get oldest
            // unpushed import details (1 query)
            System.out.println("[INVOICE] Step G.1 - Products with 0 stock to process: " + productsZeroStock);
            Map<String, DetailImportDTO> oldestUnpushedMap = DetailImportDAL.getInstance()
                    .getOldestUnpushedDetailImportsByProductIds(conn, productsZeroStock);
            System.out.println("[INVOICE] Step G.1 - Found " + oldestUnpushedMap.size()
                    + " oldest unpushed imports for price update");

            for (String productId : productsZeroStock) {
                DetailImportDTO oldestUnpushed = oldestUnpushedMap.get(productId);

                if (oldestUnpushed != null) {
                    System.out.println("[INVOICE] Step G.1 - Processing productId: " + productId);
                    // Calculate new selling price: importPrice * (1 + profitPercent / 100)
                    BigDecimal importPrice = oldestUnpushed.getPrice();
                    BigDecimal sellingPrice = importPrice.multiply(
                            BigDecimal.ONE.add(
                                    oldestUnpushed.getProfitPercent().divide(BigDecimal.valueOf(100), 2,
                                            RoundingMode.HALF_UP)));
                    System.out
                            .println("[INVOICE] Step G.1 - productId " + productId + " - Import price: " + importPrice +
                                    ", Profit %: " + oldestUnpushed.getProfitPercent() + ", New selling price: "
                                    + sellingPrice);
                    priceUpdateList.add(new PriceUpdateInfoDTO(productId, importPrice, sellingPrice));

                    // Mark this detail import as pushed
                    pushedDetailsList.add(new DetailPushedInfoDTO(productId, true));
                } else {
                    System.out.println("[INVOICE] Step G.1 - productId " + productId
                            + " - NO oldest unpushed found, skip price update");
                }
            }

            // Step G.2: Batch update prices if any (1 query)
            if (!priceUpdateList.isEmpty()) {
                System.out
                        .println("[INVOICE] Step G.2 - Number of products to update prices: " + priceUpdateList.size());
                if (!ProductDAL.getInstance().batchUpdatePriceAfterImport(conn, priceUpdateList)) {
                    throw new Exception("UPDATE_PRICE_FAIL");
                }
                System.out.println(
                        "[INVOICE] Step G.2 - Successfully updated prices for " + priceUpdateList.size() + " products");
            } else {
                System.out.println("[INVOICE] Step G.2 - No products to update prices");
            }

            // Step G.3: Batch update is_pushed for DetailImport (1 query)
            if (!pushedDetailsList.isEmpty()) {
                System.out.println(
                        "[INVOICE] Step G.3 - Number of details to mark as pushed: " + pushedDetailsList.size());
                if (!DetailImportDAL.getInstance().batchUpdateIsPushed(conn, pushedDetailsList)) {
                    throw new Exception("UPDATE_PUSHED_FAIL");
                }
                System.out.println(
                        "[INVOICE] Step G.3 - Successfully marked " + pushedDetailsList.size() + " details as pushed");

                // Step G.3.5: [OPTIMIZED] Set product stock_quantity = detail_import quantity
                // (pushed batch quantity)
                // When pushing import batch, assign the batch quantity to product stock
                // Khi đẩy lô nhập, gắn số lượng lô vào tồn kho sản phẩm
                System.out.println("[INVOICE] Step G.3.5 - Setting product stock from pushed import batches");
                Map<String, Integer> productStockToSetMap = new HashMap<>();

                for (String productId : productsZeroStock) {
                    DetailImportDTO oldestUnpushed = oldestUnpushedMap.get(productId);
                    if (oldestUnpushed != null) {
                        productStockToSetMap.put(productId, oldestUnpushed.getQuantity());
                        System.out.println("[INVOICE] Step G.3.5 - productId: " + productId + " -> set stock to: "
                                + oldestUnpushed.getQuantity());
                    }
                }

                if (!productStockToSetMap.isEmpty()) {
                    if (!ProductDAL.getInstance().batchSetStockQuantity(conn, productStockToSetMap)) {
                        throw new Exception("SET_STOCK_FAIL");
                    }
                    System.out
                            .println("[INVOICE] Step G.3.5 - Successfully set stock for " + productStockToSetMap.size()
                                    + " products from pushed import batches");
                } else {
                    System.out.println("[INVOICE] Step G.3.5 - No products to update stock from pushed batches");
                }

                // Step G.4: [OPTIMIZED] Batch get import IDs and check/update their status
                // (minimal queries)
                // Get unique importIds affected by this sale
                System.out.println("[INVOICE] Step G.4 - Products with 0 stock: " + productsZeroStock);
                int completedStatusIdImport = StatusBUS.getInstance()
                        .getByTypeAndStatusName(StatusType.IMPORT, Status.Import.COMPLETED).getId();
                Map<String, Integer> productToImportIdMap = DetailImportDAL.getInstance()
                        .getIncompleteImportIdsByProductIds(conn, productsZeroStock, completedStatusIdImport);
                System.out.println("[INVOICE] Step G.4 - Product to ImportId mapping: " + productToImportIdMap);

                // Collect unique importIds
                Set<Integer> uniqueImportIds = new HashSet<>(productToImportIdMap.values());

                // DEBUG: Log unique import IDs
                System.out.println("[INVOICE] Step G.4 - Unique Import IDs to check: " + uniqueImportIds);

                // Update status for imports where all details are now pushed
                for (Integer importId : uniqueImportIds) {
                    System.out.println("[INVOICE] Step G.4 - Checking importId: " + importId);

                    // Check if all details of this import are now pushed
                    boolean allPushed = DetailImportDAL.getInstance().areAllDetailsPushed(conn, importId);
                    System.out.println(
                            "[INVOICE] Step G.4 - importId " + importId + " - All details pushed: " + allPushed);

                    if (allPushed) {
                        System.out
                                .println("[INVOICE] Step G.4 - importId " + importId + " - Fetching COMPLETED status");
                        int completedImportStatusId = StatusBUS.getInstance()
                                .getByTypeAndStatusName(StatusType.IMPORT, Status.Import.COMPLETED).getId();
                        System.out.println("[INVOICE] Step G.4 - importId " + importId + " - COMPLETED status ID: "
                                + completedImportStatusId);

                        boolean updateSuccess = ImportDAL.getInstance().updateStatus(conn, importId,
                                completedImportStatusId);
                        System.out.println("[INVOICE] Step G.4 - importId " + importId + " - Update status result: "
                                + updateSuccess);

                        if (!updateSuccess) {
                            System.out
                                    .println("[INVOICE] Step G.4 - ERROR: Failed to update import status for importId "
                                            + importId);
                            throw new Exception("UPDATE_IMPORT_STATUS_FAIL");
                        }
                    } else {
                        System.out.println("[INVOICE] Step G.4 - importId " + importId
                                + " - Skipped (not all details pushed yet)");
                    }
                }
                System.out.println("[INVOICE] Step G.4 - Completed import status updates for " + uniqueImportIds.size()
                        + " imports");
            }

            // Commit transaction
            conn.commit();

            finalMessage = "Bán hàng thành công!";
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
            if ("INVOICE_MASTER_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi tạo phiếu bán.");
            } else if ("DETAIL_INVOICE_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi tạo chi tiết phiếu bán.");
            } else if ("INSUFFICIENT_STOCK".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Số lượng sản phẩm không đủ để bán.");
            } else if ("UPDATE_STOCK_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi cập nhật tồn kho sản phẩm.");
            } else if ("UPDATE_PRICE_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi cập nhật giá bán mới.");
            } else if ("UPDATE_PUSHED_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi cập nhật trạng thái đẩy giá.");
            } else if ("SET_STOCK_FAIL".equals(errCode)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, "Lỗi khi cập nhật tồn kho từ lô đẩy.");
            } else if ("UPDATE_IMPORT_STATUS_FAIL".equals(errCode)) {
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
     * Lấy dữ liệu hóa đơn để in PDF
     */
    public InvoicePDFDTO getInvoiceForPDF(int invoiceId) {
        return InvoiceDAL.getInstance().getInvoiceForPDF(invoiceId);
    }
}
