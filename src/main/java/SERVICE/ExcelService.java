package SERVICE;

import BUS.CategoryBUS;
import BUS.EmployeeBUS;
import BUS.ProductBUS;
import DTO.BUSResult;
import ENUM.BUSOperationResult;
import DTO.EmployeeExcelDTO;
import DTO.HrStatisticDTO;
import DTO.ProductDTO;
import DTO.StatisticDTO;
import UTILS.ValidationUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.awt.Desktop;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
     * Export báo cáo thống kê sản phẩm theo bộ lọc hiện hành.
     */
    public void exportBusinessProductStatisticReport(
            List<StatisticDTO.ProductRevenue> productRevenuesList,
            LocalDate start,
            LocalDate end,
            String viewByLabel) throws IOException {
        StatisticDTO dto = new StatisticDTO();
        dto.setProductRevenues(productRevenuesList);
        exportBusinessStatisticWorkbook(dto, start, end, viewByLabel);
    }

    /**
     * Export báo cáo thống kê kinh doanh đầy đủ theo các tab của module thống kê.
     */
    public void exportBusinessStatisticWorkbook(
            StatisticDTO dto,
            LocalDate start,
            LocalDate end,
            String viewByLabel) throws IOException {
        exportBusinessStatisticWorkbook(dto, start, end, viewByLabel, null);
    }

    public void exportBusinessStatisticWorkbook(
            StatisticDTO dto,
            LocalDate start,
            LocalDate end,
            String viewByLabel,
            File targetFile) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));

        createOverviewSheet(workbook, dto, start, end, viewByLabel);
        createRevenueSheet(workbook, dto, start, end, viewByLabel);
        createCostSheet(workbook, dto, start, end, viewByLabel);
        createProfitSheet(workbook, dto, start, end, viewByLabel);
        createSalesSheet(workbook, dto, start, end, viewByLabel);
        createReportSheet(workbook, dto, start, end, viewByLabel);

        File outputFile = targetFile != null
                ? targetFile
                : new File("ThongKe_LegoStore_Full_Statistic_Report_" + timeStamp + ".xlsx");
        fileHandler.saveAndOpenFile(outputFile, workbook);
    }

    private void createOverviewSheet(XSSFWorkbook workbook, StatisticDTO dto, LocalDate start, LocalDate end,
                                     String viewByLabel) {
        Sheet sheet = workbook.createSheet("TongQuan");
        fillCommonHeader(sheet, "Tổng quan", start, end, viewByLabel);

        Row header = sheet.createRow(5);
        header.createCell(0).setCellValue("KPI");
        header.createCell(1).setCellValue("Giá trị");
        Row r1 = sheet.createRow(6);
        r1.createCell(0).setCellValue("Tổng doanh thu");
        r1.createCell(1).setCellValue(dto.getTotalRevenue().doubleValue());
        Row r2 = sheet.createRow(7);
        r2.createCell(0).setCellValue("Tổng chi");
        r2.createCell(1).setCellValue(dto.getTotalCost().doubleValue());
        Row r3 = sheet.createRow(8);
        r3.createCell(0).setCellValue("Lợi nhuận");
        r3.createCell(1).setCellValue(dto.getProfit().doubleValue());
        Row r4 = sheet.createRow(9);
        r4.createCell(0).setCellValue("Số hóa đơn");
        r4.createCell(1).setCellValue(dto.getTotalInvoiceCount());

        int dataStart = 12;
        Row timelineHeader = sheet.createRow(dataStart);
        timelineHeader.createCell(0).setCellValue("Kỳ");
        timelineHeader.createCell(1).setCellValue("Doanh thu");
        timelineHeader.createCell(2).setCellValue("Chi phí");
        int row = dataStart + 1;
        for (StatisticDTO.ProfitPoint p : dto.getProfitTimeline()) {
            Row d = sheet.createRow(row++);
            d.createCell(0).setCellValue(p.getPeriod());
            d.createCell(1).setCellValue(p.getRevenue().doubleValue());
            d.createCell(2).setCellValue(p.getTotalCost().doubleValue());
        }
        if (row > dataStart + 1) {
            createLineChart(sheet, "Doanh thu vs Chi phí", dataStart, row - 1, 0, 1, 2, 4, 20, 14, 36);
        }
        autosize(sheet, 3);
    }

    private void createRevenueSheet(XSSFWorkbook workbook, StatisticDTO dto, LocalDate start, LocalDate end,
                                    String viewByLabel) {
        Sheet sheet = workbook.createSheet("DoanhThu");
        fillCommonHeader(sheet, "Doanh thu", start, end, viewByLabel);

        int headerRow = 5;
        Row h = sheet.createRow(headerRow);
        h.createCell(0).setCellValue("Kỳ");
        h.createCell(1).setCellValue("Doanh thu");
        h.createCell(2).setCellValue("Số hóa đơn");
        int row = headerRow + 1;
        for (StatisticDTO.RevenuePoint p : dto.getRevenueTimeline()) {
            Row d = sheet.createRow(row++);
            d.createCell(0).setCellValue(p.getPeriod());
            d.createCell(1).setCellValue(p.getRevenue().doubleValue());
            d.createCell(2).setCellValue(p.getInvoiceCount());
        }
        if (row > headerRow + 1) {
            createLineChart(sheet, "Doanh thu theo kỳ", headerRow, row - 1, 0, 1, -1, 4, 10, 14, 28);
        }
        autosize(sheet, 3);
    }

    private void createCostSheet(XSSFWorkbook workbook, StatisticDTO dto, LocalDate start, LocalDate end,
                                 String viewByLabel) {
        Sheet sheet = workbook.createSheet("Chi");
        fillCommonHeader(sheet, "Chi phí", start, end, viewByLabel);

        int headerRow = 5;
        Row h = sheet.createRow(headerRow);
        h.createCell(0).setCellValue("Kỳ");
        h.createCell(1).setCellValue("Chi nhập hàng");
        h.createCell(2).setCellValue("Chi lương");
        h.createCell(3).setCellValue("Tổng chi");
        int row = headerRow + 1;
        for (StatisticDTO.ProfitPoint p : dto.getProfitTimeline()) {
            Row d = sheet.createRow(row++);
            d.createCell(0).setCellValue(p.getPeriod());
            d.createCell(1).setCellValue(p.getImportCost().doubleValue());
            d.createCell(2).setCellValue(p.getSalaryCost().doubleValue());
            d.createCell(3).setCellValue(p.getTotalCost().doubleValue());
        }
        if (row > headerRow + 1) {
            createLineChart(sheet, "Chi phí theo kỳ", headerRow, row - 1, 0, 3, -1, 5, 10, 16, 28);
        }
        autosize(sheet, 4);
    }

    private void createProfitSheet(XSSFWorkbook workbook, StatisticDTO dto, LocalDate start, LocalDate end,
                                   String viewByLabel) {
        Sheet sheet = workbook.createSheet("LoiNhuan");
        fillCommonHeader(sheet, "Lợi nhuận", start, end, viewByLabel);

        int headerRow = 5;
        Row h = sheet.createRow(headerRow);
        h.createCell(0).setCellValue("Kỳ");
        h.createCell(1).setCellValue("Doanh thu");
        h.createCell(2).setCellValue("Tổng chi");
        h.createCell(3).setCellValue("Lợi nhuận");
        int row = headerRow + 1;
        for (StatisticDTO.ProfitPoint p : dto.getProfitTimeline()) {
            Row d = sheet.createRow(row++);
            d.createCell(0).setCellValue(p.getPeriod());
            d.createCell(1).setCellValue(p.getRevenue().doubleValue());
            d.createCell(2).setCellValue(p.getTotalCost().doubleValue());
            d.createCell(3).setCellValue(p.getProfit().doubleValue());
        }
        if (row > headerRow + 1) {
            createLineChart(sheet, "Lợi nhuận theo kỳ", headerRow, row - 1, 0, 3, -1, 5, 10, 16, 28);
        }
        autosize(sheet, 4);
    }

    private void createSalesSheet(XSSFWorkbook workbook, StatisticDTO dto, LocalDate start, LocalDate end,
                                  String viewByLabel) {
        Sheet sheet = workbook.createSheet("DoanhSo");
        fillCommonHeader(sheet, "Doanh số", start, end, viewByLabel);

        int headerRow = 5;
        Row h = sheet.createRow(headerRow);
        h.createCell(0).setCellValue("Mã sản phẩm");
        h.createCell(1).setCellValue("Tên sản phẩm");
        h.createCell(2).setCellValue("Danh mục");
        h.createCell(3).setCellValue("Số lượng bán");
        int row = headerRow + 1;
        for (StatisticDTO.ProductRevenue p : dto.getProductRevenues()) {
            Row d = sheet.createRow(row++);
            d.createCell(0).setCellValue(p.getProductId());
            d.createCell(1).setCellValue(p.getProductName());
            d.createCell(2).setCellValue(p.getCategoryName());
            d.createCell(3).setCellValue(p.getTotalQuantity());
        }
        if (row > headerRow + 1) {
            createBarChart(sheet, "Top sản phẩm theo số lượng", headerRow, row - 1, 1, 3, 6, 10, 18, 30);
        }
        autosize(sheet, 4);
    }

    private void createReportSheet(XSSFWorkbook workbook, StatisticDTO dto, LocalDate start, LocalDate end,
                                   String viewByLabel) {
        Sheet sheet = workbook.createSheet("BaoCao");
        fillCommonHeader(sheet, "Báo cáo tổng hợp", start, end, viewByLabel);

        Row header = sheet.createRow(5);
        header.createCell(0).setCellValue("Chỉ số");
        header.createCell(1).setCellValue("Giá trị");
        String profitRate = dto.getTotalRevenue().compareTo(BigDecimal.ZERO) == 0
                ? "0%"
                : dto.getProfit()
                .multiply(BigDecimal.valueOf(100))
                .divide(dto.getTotalRevenue(), 1, RoundingMode.HALF_UP) + "%";
        String[][] rows = new String[][]{
                {"Tổng doanh thu", ValidationUtils.getInstance().formatCurrency(dto.getTotalRevenue())},
                {"Tổng chi phí", ValidationUtils.getInstance().formatCurrency(dto.getTotalCost())},
                {"Lợi nhuận", ValidationUtils.getInstance().formatCurrency(dto.getProfit())},
                {"Tỷ lệ lợi nhuận", profitRate},
                {"Tổng hóa đơn", String.valueOf(dto.getTotalInvoiceCount())},
                {"Số loại sản phẩm có doanh số", String.valueOf(dto.getProductRevenues().size())}
        };
        int rowIdx = 6;
        for (String[] item : rows) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item[0]);
            row.createCell(1).setCellValue(item[1]);
        }
        autosize(sheet, 2);
    }

    private void fillCommonHeader(Sheet sheet, String title, LocalDate start, LocalDate end, String viewByLabel) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Báo cáo thống kê - " + title);
        Row periodRow = sheet.createRow(1);
        periodRow.createCell(0).setCellValue("Kỳ lọc: " + start.format(fmt) + " - " + end.format(fmt));
        Row viewRow = sheet.createRow(2);
        viewRow.createCell(0).setCellValue("Xem theo: " + viewByLabel);
        Row exportAtRow = sheet.createRow(3);
        exportAtRow.createCell(0).setCellValue(
                "Thời gian xuất: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
    }

    private void createLineChart(Sheet sheet, String title, int headerRow, int lastDataRow, int categoryCol,
                                 int series1Col, int series2Col,
                                 int fromCol, int fromRow, int toCol, int toRow) {
        XSSFSheet xssfSheet = (XSSFSheet) sheet;
        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, fromCol, fromRow, toCol, toRow);
        org.apache.poi.xssf.usermodel.XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(org.apache.poi.xddf.usermodel.chart.AxisCrosses.AUTO_ZERO);

        XDDFChartData data = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
                xssfSheet, new CellRangeAddress(headerRow + 1, lastDataRow, categoryCol, categoryCol));
        XDDFNumericalDataSource<Double> values1 = XDDFDataSourcesFactory.fromNumericCellRange(
                xssfSheet, new CellRangeAddress(headerRow + 1, lastDataRow, series1Col, series1Col));
        XDDFChartData.Series s1 = data.addSeries(categories, values1);
        s1.setTitle(sheet.getRow(headerRow).getCell(series1Col).getStringCellValue(), null);

        if (series2Col >= 0) {
            XDDFNumericalDataSource<Double> values2 = XDDFDataSourcesFactory.fromNumericCellRange(
                    xssfSheet, new CellRangeAddress(headerRow + 1, lastDataRow, series2Col, series2Col));
            XDDFChartData.Series s2 = data.addSeries(categories, values2);
            s2.setTitle(sheet.getRow(headerRow).getCell(series2Col).getStringCellValue(), null);
        }
        chart.plot(data);
    }

    private void createBarChart(Sheet sheet, String title, int headerRow, int lastDataRow, int categoryCol,
                                int valueCol, int fromCol, int fromRow, int toCol, int toRow) {
        XSSFSheet xssfSheet = (XSSFSheet) sheet;
        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, fromCol, fromRow, toCol, toRow);
        org.apache.poi.xssf.usermodel.XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setBarDirection(BarDirection.COL);

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
                xssfSheet, new CellRangeAddress(headerRow + 1, lastDataRow, categoryCol, categoryCol));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
                xssfSheet, new CellRangeAddress(headerRow + 1, lastDataRow, valueCol, valueCol));
        XDDFChartData.Series series = data.addSeries(categories, values);
        series.setTitle(sheet.getRow(headerRow).getCell(valueCol).getStringCellValue(), null);
        chart.plot(data);
    }

    private void autosize(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Export báo cáo thống kê nhân sự theo tháng/năm.
     */
    public void exportHrStatisticWorkbook(HrStatisticDTO dto, int month, int year, File targetFile) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        createHrOverviewSheet(workbook, dto, month, year);
        createHrAttendanceSheet(workbook, dto, month, year);
        createHrLeaveSheet(workbook, dto, month, year);
        createHrFineRewardSheet(workbook, dto, month, year);
        createHrSalarySheet(workbook, dto, month, year);
        createHrReportSheet(workbook, dto, month, year);

        File outputFile = targetFile != null
                ? targetFile
                : new File("ThongKe_NhanSu_" + month + "_" + year + ".xlsx");
        fileHandler.saveAndOpenFile(outputFile, workbook);
    }

    private void createHrOverviewSheet(XSSFWorkbook workbook, HrStatisticDTO dto, int month, int year) {
        Sheet sheet = workbook.createSheet("TongQuanNhanSu");
        fillHrHeader(sheet, "Tổng quan nhân sự", month, year);

        Row h = sheet.createRow(5);
        h.createCell(0).setCellValue("Chỉ số");
        h.createCell(1).setCellValue("Giá trị");
        Row r1 = sheet.createRow(6);
        r1.createCell(0).setCellValue("Tổng nhân sự");
        r1.createCell(1).setCellValue(dto.getTotalEmployees());
        Row r2 = sheet.createRow(7);
        r2.createCell(0).setCellValue("Nhân sự mới");
        r2.createCell(1).setCellValue(dto.getNewEmployees());
        Row r3 = sheet.createRow(8);
        r3.createCell(0).setCellValue("Tổng lương đã trả");
        r3.createCell(1).setCellValue(ValidationUtils.getInstance().formatCurrency(dto.getTotalPaidSalary()));
        autosize(sheet, 2);
    }

    private void createHrAttendanceSheet(XSSFWorkbook workbook, HrStatisticDTO dto, int month, int year) {
        Sheet sheet = workbook.createSheet("ChamCong");
        fillHrHeader(sheet, "Chấm công", month, year);
        Row h = sheet.createRow(5);
        h.createCell(0).setCellValue("Họ tên");
        h.createCell(1).setCellValue("Phòng ban");
        h.createCell(2).setCellValue("Số ca");
        h.createCell(3).setCellValue("Giờ làm");
        h.createCell(4).setCellValue("Giờ OT");
        int row = 6;
        for (HrStatisticDTO.AttendanceRow item : dto.getAttendanceRows()) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(item.getFullName());
            r.createCell(1).setCellValue(item.getDeptName());
            r.createCell(2).setCellValue(item.getSessionCount());
            r.createCell(3).setCellValue(item.getTotalWork().doubleValue());
            r.createCell(4).setCellValue(item.getTotalOt().doubleValue());
        }
        autosize(sheet, 5);
    }

    private void createHrLeaveSheet(XSSFWorkbook workbook, HrStatisticDTO dto, int month, int year) {
        Sheet sheet = workbook.createSheet("NghiPhep");
        fillHrHeader(sheet, "Nghỉ phép", month, year);
        Row h = sheet.createRow(5);
        h.createCell(0).setCellValue("Họ tên");
        h.createCell(1).setCellValue("Loại nghỉ");
        h.createCell(2).setCellValue("Từ ngày");
        h.createCell(3).setCellValue("Đến ngày");
        h.createCell(4).setCellValue("Số ngày");
        h.createCell(5).setCellValue("Trạng thái");
        int row = 6;
        for (HrStatisticDTO.LeaveRow item : dto.getLeaveRows()) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(item.getFullName());
            r.createCell(1).setCellValue(item.getLeaveType());
            r.createCell(2).setCellValue(item.getStartDate());
            r.createCell(3).setCellValue(item.getEndDate());
            r.createCell(4).setCellValue(item.getDays());
            r.createCell(5).setCellValue(item.getStatus());
        }
        autosize(sheet, 6);
    }

    private void createHrFineRewardSheet(XSSFWorkbook workbook, HrStatisticDTO dto, int month, int year) {
        Sheet sheet = workbook.createSheet("ThuongPhat");
        fillHrHeader(sheet, "Khen thưởng - Kỷ luật", month, year);
        Row h = sheet.createRow(5);
        h.createCell(0).setCellValue("Mã NV");
        h.createCell(1).setCellValue("Họ tên");
        h.createCell(2).setCellValue("Phòng ban");
        h.createCell(3).setCellValue("Chức vụ");
        h.createCell(4).setCellValue("Loại");
        h.createCell(5).setCellValue("Số tiền");
        h.createCell(6).setCellValue("Ngày");
        int row = 6;
        for (HrStatisticDTO.FineRewardRow item : dto.getFineRewardRows()) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(item.getEmployeeCode());
            r.createCell(1).setCellValue(item.getFullName());
            r.createCell(2).setCellValue(item.getDepartmentName());
            r.createCell(3).setCellValue(item.getPositionName());
            r.createCell(4).setCellValue(item.getFineLevel());
            r.createCell(5).setCellValue(item.getAmount().doubleValue());
            r.createCell(6).setCellValue(item.getCreatedAt());
        }
        autosize(sheet, 7);
    }

    private void createHrSalarySheet(XSSFWorkbook workbook, HrStatisticDTO dto, int month, int year) {
        Sheet sheet = workbook.createSheet("Luong");
        fillHrHeader(sheet, "Thống kê lương", month, year);
        Row h = sheet.createRow(5);
        h.createCell(0).setCellValue("Họ tên");
        h.createCell(1).setCellValue("Phòng ban");
        h.createCell(2).setCellValue("Lương cơ bản");
        h.createCell(3).setCellValue("Lương NET");
        h.createCell(4).setCellValue("Bảo hiểm");
        h.createCell(5).setCellValue("Ngày công");
        int row = 6;
        for (HrStatisticDTO.SalaryRow item : dto.getSalaryRows()) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(item.getFullName());
            r.createCell(1).setCellValue(item.getDeptName());
            r.createCell(2).setCellValue(item.getBaseSalary().doubleValue());
            r.createCell(3).setCellValue(item.getNetSalary().doubleValue());
            r.createCell(4).setCellValue(item.getTotalInsurance().doubleValue());
            r.createCell(5).setCellValue(item.getActualWorkDays().doubleValue());
        }
        autosize(sheet, 6);
    }

    private void createHrReportSheet(XSSFWorkbook workbook, HrStatisticDTO dto, int month, int year) {
        Sheet sheet = workbook.createSheet("BaoCaoNhanSu");
        fillHrHeader(sheet, "Báo cáo nhân sự", month, year);
        Row h = sheet.createRow(5);
        h.createCell(0).setCellValue("Chỉ số");
        h.createCell(1).setCellValue("Giá trị");
        String[][] rows = new String[][]{
                {"Tổng nhân sự", String.valueOf(dto.getTotalEmployees())},
                {"Nhân sự mới", String.valueOf(dto.getNewEmployees())},
                {"Tổng lương đã trả", ValidationUtils.getInstance().formatCurrency(dto.getTotalPaidSalary())},
                {"Tổng lượt chấm công", String.valueOf(dto.getAttendanceStat().getTotalSessions())},
                {"Tổng đơn nghỉ phép", String.valueOf(dto.getLeaveStat().getTotalRequests())}
        };
        int row = 6;
        for (String[] item : rows) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(item[0]);
            r.createCell(1).setCellValue(item[1]);
        }
        autosize(sheet, 2);
    }

    private void fillHrHeader(Sheet sheet, String title, int month, int year) {
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Báo cáo thống kê nhân sự - " + title);
        Row periodRow = sheet.createRow(1);
        periodRow.createCell(0).setCellValue("Tháng/Năm: " + month + "/" + year);
        Row exportAtRow = sheet.createRow(2);
        exportAtRow.createCell(0).setCellValue(
                "Thời gian xuất: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
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
