
package SERVICE;

import BUS.AccountBUS;
import BUS.CategoryBUS;
import BUS.EmployeeBUS;
import BUS.ProductBUS;
import BUS.RoleBUS;
import DTO.AccountDTO;
import DTO.EmployeeDTO;
import DTO.ProductDTO;
import DTO.RoleDTO;
import DTO.StatisticDTO;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
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
     * Export danh sách nhân viên
     */
    public void exportEmployeeToFileExcel() throws IOException {

        List<EmployeeDTO> employees = EmployeeBUS.getInstance().getAll();
        RoleBUS roleBUS = RoleBUS.getInstance();
        AccountBUS accountBUS = AccountBUS.getInstance();
        ValidationUtils validate = ValidationUtils.getInstance();

        List<String> headers = Arrays.asList(
                "ID", "First Name", "Last Name", "Date Of Birth", "Role Name", "Salary", "Final Salary", "Status");

        exportGeneric(
                "employee.xlsx",
                headers,
                employees,
                (row, emp) -> {
                    // Get role from account, not from employee
                    RoleDTO role = null;
                    if (emp.getAccountId() != null && emp.getAccountId() > 0) {
                        AccountDTO account = accountBUS.getById(emp.getAccountId());
                        if (account != null && account.getRoleId() > 0) {
                            role = roleBUS.getById(account.getRoleId());
                        }
                    }
                    row.createCell(0).setCellValue(emp.getId());
                    row.createCell(1).setCellValue(emp.getFirstName());
                    row.createCell(2).setCellValue(emp.getLastName());
                    row.createCell(3).setCellValue(validate.formatDateTime(emp.getDateOfBirth()));
                    row.createCell(4).setCellValue(role != null ? role.getName() : "");
                });
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

    // ========== IMPORT METHODS ==========

    public void ImportSheet(String importData, Stage stage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel để nhập dữ liệu");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showOpenDialog(stage);

        if (file == null) {
            return; // Người dùng không chọn file
        }

        // Kiểm tra đúng định dạng
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            NotificationUtils.showErrorAlert("Vui lòng chọn file Excel (.xlsx)", "Thông báo");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (importData.equalsIgnoreCase("products")) {
                importToProducts(sheet);
            }

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtils.showErrorAlert("Không thể mở file Excel: " + e.getMessage(), "Lỗi");
        }
    }

    private void importToProducts(Sheet sheet) {
        ArrayList<ProductDTO> list = returnListProduct(sheet, new ArrayList<>());

        if (list.isEmpty()) {
            return;
        }

        if (UiUtils.gI().showConfirmAlert("Bạn chắc chắn muốn thêm sản phẩm bằng Excel?", "Thông báo")) {
            int deleteResult = ProductBUS.getInstance().insertListProductExcel(list);
            switch (deleteResult) {
                case 1 -> NotificationUtils.showInfoAlert("Thêm sản phẩm thành công.", "Thông báo");
                case 2 -> NotificationUtils.showErrorAlert("Danh sách rỗng.", "Thông báo");
                case 3 -> NotificationUtils.showErrorAlert("Dữ liệu đầu vào không hợp lệ.", "Thông báo");
                case 4 -> NotificationUtils.showErrorAlert("Thể loại không hợp lệ hoặc đã bị xóa.", "Thông báo");
                case 5 -> NotificationUtils.showErrorAlert("Tên sản phẩm trong hệ thống đã tồn tại.", "Thông báo");
                case 6 ->
                    NotificationUtils.showErrorAlert("Có lỗi khi thêm danh sách sản phẩm qua Excel.", "Thông báo");
                case 7 -> NotificationUtils.showErrorAlert("Lỗi cơ sở dữ liệu khi insert.", "Thông báo");
                default -> NotificationUtils.showErrorAlert("Lỗi không xác định, vui lòng thử lại sau.", "Thông báo");
            }
        }
    }

    // VALIDATE ON PRODUCT BUS
    private ArrayList<ProductDTO> returnListProduct(Sheet sheet, ArrayList<ProductDTO> list) {
        if (list == null)
            return new ArrayList<>();
        list.clear(); // Clear luôn nếu không null

        ArrayList<ProductDTO> tempList = new ArrayList<>();
        StringBuilder errorMessages = new StringBuilder();
        int errorCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue; // Bỏ qua tiêu đề
            if (isRowEmpty(row))
                continue; // Bỏ qua dòng trống

            try {
                Cell nameCell = row.getCell(1);
                Cell descCell = row.getCell(2);
                Cell categoryCell = row.getCell(3);
                Cell statusCell = row.getCell(4);

                String name = (nameCell != null) ? nameCell.getStringCellValue().trim() : "";
                String description = (descCell != null) ? descCell.getStringCellValue().trim() : "";

                if (name.isEmpty()) {
                    if (handleError(errorMessages, row.getRowNum(), "Tên sản phẩm không được để trống.", ++errorCount))
                        break;
                    continue;
                }
                if (name.length() > 148) {
                    if (handleError(errorMessages, row.getRowNum(), "Tên sản phẩm không được quá 148 ký tự.",
                            ++errorCount))
                        break;
                    continue;
                }
                if (description.length() > 65400) {
                    if (handleError(errorMessages, row.getRowNum(), "Mô tả không được quá 65k4 ký tự.", ++errorCount))
                        break;
                    continue;
                }

                int categoryId;
                try {
                    categoryId = (int) categoryCell.getNumericCellValue();
                } catch (Exception e) {
                    if (handleError(errorMessages, row.getRowNum(), "Thể loại không hợp lệ (phải là số).",
                            ++errorCount))
                        break;
                    continue;
                }
                // if (categoryId < 0 || !CategoryBUS.getInstance().isValidCategory(categoryId))
                // {
                // if (handleError(errorMessages, row.getRowNum(), "Thể loại không hợp lệ hoặc
                // đã bị xóa.",
                // ++errorCount))
                // break;
                // continue;
                // }

                int statusInt;
                try {
                    statusInt = (int) statusCell.getNumericCellValue();
                } catch (Exception e) {
                    if (handleError(errorMessages, row.getRowNum(), "Trạng thái không hợp lệ (phải là 0 hoặc 1).",
                            ++errorCount))
                        break;
                    continue;
                }
                if (statusInt != 0 && statusInt != 1) {
                    if (handleError(errorMessages, row.getRowNum(), "Trạng thái chỉ được là 0 hoặc 1.", ++errorCount))
                        break;
                    continue;
                }

                // tempList.add(new ProductDTO(null, name, 0, null, statusInt == 1, description,
                // null, categoryId));

            } catch (Exception e) {
                if (handleError(errorMessages, row.getRowNum(), "Lỗi không xác định: " + e.getMessage(), ++errorCount))
                    break;
            }
        }

        if (errorMessages.length() > 0) {
            if (errorCount > 20) {
                errorMessages.append("Và một số lỗi khác không thể hiển thị.\n");
            }
            NotificationUtils.showErrorAlert(errorMessages.toString(), "Thông báo");
            return new ArrayList<>();
        }

        list.addAll(tempList);
        return list;
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

}
