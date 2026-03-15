package SERVICE;

import BUS.CategoryBUS;
import BUS.EmployeeBUS;
import BUS.ProductBUS;
import DTO.BUSResult;
import ENUM.BUSOperationResult;
import DTO.EmployeeExcelDTO;
import DTO.ProductDTO;
import DTO.StatisticDTO;
import UTILS.ValidationUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.awt.Desktop;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

import static java.time.LocalDate.now;

public class ExcelService {
    private static final ExcelService INSTANCE = new ExcelService();
    private final ExcelFileHandler fileHandler = new ExcelFileHandler();

    private ExcelService() {
    }

    public static ExcelService getInstance() {
        return INSTANCE;
    }

    /**
     * Generic method để export dữ liệu với custom header và row mapper
     * 
     * @param fileName  tên file cần lưu
     * @param headers   danh sách header (cột)
     * @param data      danh sách dữ liệu cần export
     * @param rowMapper function để map object sang Row
     * @param <T>       kiểu dữ liệu generic
     */
    public <T> void exportGeneric(
            String fileName,
            List<String> headers,
            List<T> data,
            BiConsumer<Row, T> rowMapper) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Source");

        // Tạo header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }

        // Tạo data rows
        int rowIndex = 1;
        for (T item : data) {
            Row dataRow = sheet.createRow(rowIndex++);
            rowMapper.accept(dataRow, item);
        }

        // Auto-size all columns
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        fileHandler.saveAndOpenFile(fileName, workbook, false);
    }

    /**
     * Export danh sách nhân viên - trả về BUSResult
     */
    public BUSResult exportEmployeeToFileExcel() {
        try {
            List<EmployeeExcelDTO> employees = EmployeeBUS.getInstance().getAllEmployeesForExcel();

            if (employees.isEmpty()) {
                return new BUSResult(BUSOperationResult.FAIL, "Không có nhân viên nào để export");
            }

            List<String> headers = Arrays.asList(
                    "ID", "Họ tên", "Giới tính", "Vị trí", "Phòng ban", "Tên tài khoản", "Lương",
                    "Trạng thái", "Mã BHYT", "Mã BHXH", "Mã BHTN",
                    "Hỗ trợ cơm", "Hỗ trợ xe", "Hỗ trợ chỗ ở", "Số người phụ thuộc");

            String folderName = "excel";
            File folder = new File(folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
            String fileName = folderName + File.separator + "employee_" + timestamp + ".xlsx";

            exportGeneric(
                    fileName,
                    headers,
                    employees,
                    (row, emp) -> {
                        row.createCell(0).setCellValue(emp.getId());
                        row.createCell(1).setCellValue(emp.getFullName());
                        row.createCell(2).setCellValue(emp.getGender() != null ? emp.getGender() : "");
                        row.createCell(3).setCellValue(emp.getPositionName());
                        row.createCell(4).setCellValue(emp.getDepartmentName());
                        row.createCell(5).setCellValue(emp.getUsername());
                        row.createCell(6).setCellValue(emp.getWage() != null ? emp.getWage().doubleValue() : 0);
                        row.createCell(7).setCellValue(emp.getStatusDescription());
                        row.createCell(8).setCellValue(emp.getHealthInsCode() != null ? emp.getHealthInsCode() : "");
                        row.createCell(9).setCellValue(emp.getSocialInsCode() != null ? emp.getSocialInsCode() : "");
                        row.createCell(10)
                                .setCellValue(emp.getUnemploymentInsCode() != null ? emp.getUnemploymentInsCode() : "");
                        row.createCell(11).setCellValue(emp.isMealSupport() ? "Có" : "Không");
                        row.createCell(12).setCellValue(emp.isTransportationSupport() ? "Có" : "Không");
                        row.createCell(13).setCellValue(emp.isAccommodationSupport() ? "Có" : "Không");
                        row.createCell(14).setCellValue(emp.getNumDependents());
                    });

            // Mở file
            File excelFile = new File(fileName);
            if (excelFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(excelFile);
            }

            return new BUSResult(BUSOperationResult.SUCCESS, "Xuất file Excel thành công!");
        } catch (IOException e) {
            System.err.println("Lỗi export Excel: " + e.getMessage());
            return new BUSResult(BUSOperationResult.FAIL, "Lỗi khi xuất file Excel: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi export Excel: " + e.getMessage());
            return new BUSResult(BUSOperationResult.DB_ERROR, "Lỗi không xác định: " + e.getMessage());
        }
    }

    public void exportProductToFileExcel() throws IOException {
        List<ProductDTO> products = ProductBUS.getInstance().getAll();
        CategoryBUS categoryBUS = CategoryBUS.getInstance();

        List<String> headers = Arrays.asList(
                "ID", "Name", "Description", "Category Name", "Status");

        exportGeneric(
                "products.xlsx",
                headers,
                products,
                (row, prod) -> {
                    String categoryName = "";
                    if (prod.getCategoryId() > 0) {
                        categoryName = categoryBUS.getById(prod.getCategoryId()) != null
                                ? categoryBUS.getById(prod.getCategoryId()).getName()
                                : "";
                    }
                    row.createCell(0).setCellValue(prod.getId());
                    row.createCell(1).setCellValue(prod.getName());
                    row.createCell(2).setCellValue(prod.getDescription());
                    row.createCell(3).setCellValue(categoryName);
                    row.createCell(4).setCellValue(prod.getStatusId());
                });
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK &&
                    !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean handleError(StringBuilder errorMessages, int rowNum, String message, int errorCount) {
        errorMessages.append("Dòng ").append(rowNum + 1).append(": ").append(message).append("\n");
        return errorCount >= 20;
    }

    /**
     * Export thống kê doanh thu theo sản phẩm
     */
    public void exportToFileExcelProductRevenues(ArrayList<StatisticDTO.ProductRevenue> productRevenuesList,
            String timestamp, LocalDate start, LocalDate end) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("TK_Product_" + now());

        // Header info
        Row headerInfoRow = sheet.createRow(0);
        headerInfoRow.createCell(0).setCellValue("Bảng thống kê doanh thu sản phẩm từ " +
                start.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +
                " đến " + end.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Row timeRow = sheet.createRow(1);
        timeRow.createCell(0).setCellValue("Thời gian xuất file: " + currentTime);

        // Table header
        Row headerRow = sheet.createRow(2);
        headerRow.createCell(0).setCellValue("Mã sản phẩm");
        headerRow.createCell(1).setCellValue("Tên sản phẩm");
        headerRow.createCell(2).setCellValue("Thể loại sản phẩm");
        headerRow.createCell(3).setCellValue("Số lượng bán ra");

        // Data rows
        int rowNum = 3;
        for (StatisticDTO.ProductRevenue item : productRevenuesList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getProductId());
            row.createCell(1).setCellValue(item.getProductName());
            row.createCell(2).setCellValue(item.getCategoryName());
            row.createCell(3).setCellValue(item.getTotalQuantity());
        }

        // Total row
        Row totalRevenueRow = sheet.createRow(rowNum + 1);
        totalRevenueRow.createCell(3).setCellValue("Tổng: ");
        int totalProductQuantity = 0;
        for (StatisticDTO.ProductRevenue item : productRevenuesList) {
            totalProductQuantity = totalProductQuantity + (item.getTotalQuantity());
        }
        totalRevenueRow.createCell(4).setCellValue(totalProductQuantity);

        // Auto-size columns
        for (int i = 1; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        String fileName = "ThongKe_LegoStore_TK_Product_" + timestamp + ".xlsx";
        fileHandler.saveAndOpenFile(fileName, workbook);
    }

    /**
     * Export thống kê doanh thu theo nhân viên theo quý
     */
    public void exportToFileExcelEmployeeRevenues(ArrayList<StatisticDTO.QuarterlyEmployeeRevenue> employeeRevenueList,
            String timestamp, String year) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("TK_Employee_" + now());

        // Header info
        Row headerInfoRow = sheet.createRow(0);
        headerInfoRow.createCell(0).setCellValue("Bảng thống kê doanh thu sản phẩm theo Quý năm " + year);

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        Row timeRow = sheet.createRow(1);
        timeRow.createCell(0).setCellValue("Thời gian xuất file: " + currentTime);

        // Table header
        Row headerRow = sheet.createRow(2);
        headerRow.createCell(0).setCellValue("Mã Nhân viên");
        headerRow.createCell(1).setCellValue("Quý 1");
        headerRow.createCell(2).setCellValue("Quý 2");
        headerRow.createCell(3).setCellValue("Quý 3");
        headerRow.createCell(4).setCellValue("Quý 4");
        headerRow.createCell(5).setCellValue("Tổng doanh thu");

        ValidationUtils validate = ValidationUtils.getInstance();

        // Data rows
        int rowNum = 3;
        for (StatisticDTO.QuarterlyEmployeeRevenue item : employeeRevenueList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getEmployeeId());
            row.createCell(1).setCellValue(validate.formatCurrency(item.getQuarter1()));
            row.createCell(2).setCellValue(validate.formatCurrency(item.getQuarter2()));
            row.createCell(3).setCellValue(validate.formatCurrency(item.getQuarter3()));
            row.createCell(4).setCellValue(validate.formatCurrency(item.getQuarter4()));
            row.createCell(5).setCellValue(validate.formatCurrency(item.getRevenue()));
        }

        // Total row
        Row totalRevenueRow = sheet.createRow(rowNum + 1);
        totalRevenueRow.createCell(4).setCellValue("Tổng: ");
        BigDecimal totalEmployeeRevenue = BigDecimal.ZERO;
        for (StatisticDTO.QuarterlyEmployeeRevenue item : employeeRevenueList) {
            totalEmployeeRevenue = totalEmployeeRevenue.add(item.getRevenue());
        }
        totalRevenueRow.createCell(5).setCellValue(validate.formatCurrency(totalEmployeeRevenue));

        // Auto-size columns
        for (int i = 1; i <= 5; i++) {
            sheet.autoSizeColumn(i);
        }

        String fileName = "ThongKe_LegoStore_TK_Employee_" + timestamp + ".xlsx";
        fileHandler.saveAndOpenFile(fileName, workbook);
    }

    /**
     * Read import data from Excel file
     * Expected columns: ProductId, Quantity, ImportPrice, ProfitPercent
     * 
     * @param file Excel file to read
     * @return List of TempDetailImportDTO with data from Excel
     */
    public List<DTO.TempDetailImportDTO> readImportDataFromExcel(File file) throws IOException {
        List<DTO.TempDetailImportDTO> importList = new ArrayList<>();
        ProductBUS productBUS = ProductBUS.getInstance();

        try (FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IOException("File Excel không có sheet nào!");
            }

            // Row 0 là header, bắt đầu từ row 1
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null)
                    continue;

                try {
                    // Column 0: ProductId
                    String productId = getCellStringValue(row, 0).trim();
                    if (productId.isEmpty())
                        continue; // Skip rows without product ID

                    // Column 1: Quantity
                    int quantity = (int) getCellNumericValue(row, 1);
                    if (quantity <= 0)
                        throw new Exception("Số lượng phải > 0");

                    // Column 2: ImportPrice
                    BigDecimal importPrice = BigDecimal.valueOf(getCellNumericValue(row, 2));
                    if (importPrice.compareTo(BigDecimal.ZERO) <= 0)
                        throw new Exception("Giá nhập phải > 0");

                    // Column 3: ProfitPercent
                    BigDecimal profitPercent = BigDecimal.valueOf(getCellNumericValue(row, 3));
                    if (profitPercent.compareTo(BigDecimal.ZERO) < 0)
                        throw new Exception("Tỷ lệ lợi nhuận không nên < 0");

                    // Get product name from database
                    ProductDTO product = productBUS.getById(productId);
                    if (product == null)
                        throw new Exception("Sản phẩm '" + productId + "' không tồn tại");

                    // Create TempDetailImportDTO
                    DTO.TempDetailImportDTO detail = new DTO.TempDetailImportDTO();
                    detail.setProductId(productId);
                    detail.setName(product.getName());
                    detail.setQuantity(quantity);
                    detail.setImportPrice(importPrice);
                    detail.setProfitPercent(profitPercent);

                    // Calculate total price
                    detail.setTotalPrice(importPrice.multiply(BigDecimal.valueOf(quantity)));

                    importList.add(detail);

                } catch (Exception e) {
                    System.err.println("Lỗi ở dòng " + (rowIndex + 1) + ": " + e.getMessage());
                    throw new IOException("Lỗi ở dòng " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }

            if (importList.isEmpty()) {
                throw new IOException("File Excel không chứa dữ liệu hợp lệ!");
            }

        }

        return importList;
    }

    /**
     * Helper method to get String value from cell
     */
    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    /**
     * Helper method to get numeric value from cell
     */
    private double getCellNumericValue(Row row, int cellIndex) throws Exception {
        Cell cell = row.getCell(cellIndex);
        if (cell == null)
            throw new Exception("Giá trị rỗng");
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                throw new Exception("Giá trị không phải số");
            }
        }
        throw new Exception("Định dạng giá trị không hợp lệ");
    }

}
