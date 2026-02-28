package SERVICE;

import DTO.BUSResult;
import DTO.ProductDisplayForImportDTO;
import ENUM.BUSOperationResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Template Generator Service
 * Tạo file Excel template có thể tái sử dụng cho các module khác nhau
 * + Tự động tạo timestamped file name
 * + Tự động open file
 * + Return BUSResult
 */
public class TemplateGeneratorService {
    private static final TemplateGeneratorService INSTANCE = new TemplateGeneratorService();

    private TemplateGeneratorService() {
    }

    public static TemplateGeneratorService getInstance() {
        return INSTANCE;
    }

    /**
     * Generates Employee Import Template
     * Dùng cho: EmployeeController.handleDownloadTemplate()
     *
     * @return BUSResult with success status and file name
     */
    public BUSResult generateEmployeeImportTemplate() {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Employee");

            // Headers: FirstName, LastName, Phone, Email, DateOfBirth, Gender, ...
            String[] headers = { "Họ đệm", "Tên", "SĐT", "Email", "Ngày sinh",
                    "Giới tính", "Mã phòng ban", "Mã vị trí", "Mã BHYT", "Mã BHXH",
                    "Mã BH thất nghiệp", "Hỗ trợ ăn uống", "Hỗ trợ đi lại",
                    "Hỗ trợ chỗ ở", "Số người phụ thuộc", "Tài khoản", "Mã vai trò" };

            // Create header row with style
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample data rows
            String[][] sampleData = {
                    { "Văn", "An", "0987123456", "van.an@gmail.com", "01/01/1995",
                            "Nam", "1", "1", "BH123456", "BHXH123456", "BN123456", "Y", "Y", "N", "2", "vanan", "2" },
                    { "Thị", "Bình", "0912345678", "bi.binh@gmail.com", "15/05/1996",
                            "Nữ", "2", "2", "0", "0", "0", "N", "Y", "Y", "0", "thibinh", "2" }
            };

            for (int rowIndex = 0; rowIndex < sampleData.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                for (int colIndex = 0; colIndex < sampleData[rowIndex].length; colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    cell.setCellValue(sampleData[rowIndex][colIndex]);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Create template folder if it doesn't exist
            String folderName = "template";
            File folder = new File(folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Save file with timestamp
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
            String fileName = "EmployeeTemplate_" + timestamp + ".xlsx";
            File file = new File(folder, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            // Open file
            if (file.exists() && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (Exception e) {
                    // Silent fail if cannot open file
                }
            }
            workbook.close();
            return new BUSResult(BUSOperationResult.SUCCESS,
                    "File mẫu nhân viên đã được tạo: " + fileName);

        } catch (IOException e) {
            System.err.println("Lỗi tạo template nhân viên: " + e.getMessage());
            return new BUSResult(BUSOperationResult.FAIL, "Lỗi tạo file mẫu: " + e.getMessage());
        }
    }

    /**
     * Generates Import (Product) Template
     * Gọi ProductBUS để lấy danh sách sản phẩm hợp lệ
     * Dùng cho: ImportController.handleDownloadTemplate()
     *
     * @return BUSResult with success status and file name
     */
    public BUSResult generateImportTemplate(List<ProductDisplayForImportDTO> validProducts) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();

            // Sheet 1: Import form cho user nhập dữ liệu
            Sheet sheet = workbook.createSheet("Import");

            // Headers
            String[] headers = { "Mã SP", "Số lượng", "Giá nhập", "Lợi nhuận (%)" };

            // Create header row with style
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample data rows
            String[][] sampleData = {
                    { "SP001", "10", "150000", "30" },
                    { "SP002", "5", "200000", "25" },
                    { "SP003", "8", "75000", "40" }
            };

            for (int rowIndex = 0; rowIndex < sampleData.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                for (int colIndex = 0; colIndex < sampleData[rowIndex].length; colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    String value = sampleData[rowIndex][colIndex];

                    try {
                        double numValue = Double.parseDouble(value);
                        cell.setCellValue(numValue);
                    } catch (NumberFormatException e) {
                        cell.setCellValue(value);
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Sheet 2: Valid Products - danh sách sản phẩm hợp lệ để copy ID
            Sheet validProductsSheet = workbook.createSheet("Valid Products");

            // Create header for valid products sheet
            String[] validProductHeaders = { "Mã SP", "Tên Sản Phẩm", "Kho", "Giá nhập" };
            Row validHeaderRow = validProductsSheet.createRow(0);

            CellStyle validHeaderStyle = workbook.createCellStyle();
            Font validHeaderFont = workbook.createFont();
            validHeaderFont.setBold(true);
            validHeaderFont.setColor(IndexedColors.WHITE.getIndex());
            validHeaderStyle.setFont(validHeaderFont);
            validHeaderStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            validHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < validProductHeaders.length; i++) {
                Cell cell = validHeaderRow.createCell(i);
                cell.setCellValue(validProductHeaders[i]);
                cell.setCellStyle(validHeaderStyle);
            }

            // Add valid products data
            for (int rowIndex = 0; rowIndex < validProducts.size(); rowIndex++) {
                ProductDisplayForImportDTO product = validProducts.get(rowIndex);
                Row row = validProductsSheet.createRow(rowIndex + 1);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(product.getId());

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(product.getName());

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(product.getStockQuantity());

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(product.getImportPrice().doubleValue());
            }

            // Auto-size columns for valid products sheet
            for (int i = 0; i < validProductHeaders.length; i++) {
                validProductsSheet.autoSizeColumn(i);
            }

            // Create template folder if it doesn't exist
            String folderName = "template";
            File folder = new File(folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Save file with timestamp
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
            String fileName = "ImportTemplate_" + timestamp + ".xlsx";
            File file = new File(folder, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            // Open file
            if (file.exists() && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (Exception e) {
                    // Silent fail if cannot open file
                }
            }
            workbook.close();
            return new BUSResult(BUSOperationResult.SUCCESS,
                    "File mẫu nhập hàng đã được tạo: " + fileName);

        } catch (IOException e) {
            System.err.println("Lỗi tạo template nhập hàng: " + e.getMessage());
            return new BUSResult(BUSOperationResult.FAIL, "Lỗi tạo file mẫu: " + e.getMessage());
        }
    }
}
