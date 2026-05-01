package GUI;

import BUS.StatisticBUS;
import DTO.BUSResult;
import DTO.StatisticDTO;
import DTO.StatisticDTO.CategoryRevenue;
import DTO.StatisticDTO.ProductRevenue;
import DTO.StatisticDTO.ProfitPoint;
import DTO.StatisticDTO.RevenuePoint;
import ENUM.BUSOperationResult;
import ENUM.PermissionKey;
import ENUM.ViewBy;
import INTERFACE.IController;
import SERVICE.ExcelService;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class StatisticController implements IController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Lưu trạng thái bộ lọc giữa các lần điều hướng module. */
    private static String savedFromDate = null;
    private static String savedToDate = null;
    private static ViewBy savedViewBy = null;
    private StatisticDTO lastLoadedStatistic = null;
    private boolean canView = true;

    // ── Bộ lọc chung ──────────────────────────────────────
    @FXML
    private TextField fromDate;
    @FXML
    private TextField toDate;
    @FXML
    private ComboBox<ViewBy> viewByCombo;
    @FXML
    private Button btnFilter;
    @FXML
    private Button btnRefresh;
    @FXML
    private Button btnExport;

    // ── Tab 1: Tổng quan ──────────────────────────────────
    @FXML
    private Label lblOverviewRevenue;
    @FXML
    private Label lblOverviewCost;
    @FXML
    private Label lblOverviewImportCost;
    @FXML
    private Label lblOverviewSalaryCost;
    @FXML
    private Label lblOverviewProfit;
    @FXML
    private Label lblOverviewInvoiceCount;
    @FXML
    private LineChart<String, Number> overviewTrendChart;
    @FXML
    private BarChart<String, Number> overviewCategoryChart;

    // ── Tab 2: Doanh thu ──────────────────────────────────
    @FXML
    private Label lblTotalRevenue;
    @FXML
    private Label lblTotalInvoices;
    @FXML
    private Label lblAvgInvoice;
    @FXML
    private LineChart<String, Number> revenueLineChart;
    @FXML
    private BarChart<String, Number> revenueCategoryChart;
    @FXML
    private TableView<RevenuePoint> tblRevenue;
    @FXML
    private TableColumn<RevenuePoint, String> colRevPeriod;
    @FXML
    private TableColumn<RevenuePoint, String> colRevAmount;
    @FXML
    private TableColumn<RevenuePoint, String> colRevCount;

    // ── Tab 3: Chi ────────────────────────────────────────
    @FXML
    private Label lblTotalCost;
    @FXML
    private Label lblImportCost;
    @FXML
    private Label lblSalaryCost;
    @FXML
    private Label lblImportCount;
    @FXML
    private BarChart<String, Number> costTimelineChart;
    @FXML
    private BarChart<String, Number> supplierCostChart;
    @FXML
    private TableView<ProfitPoint> tblCost;
    @FXML
    private TableColumn<ProfitPoint, String> colCostPeriod;
    @FXML
    private TableColumn<ProfitPoint, String> colCostImport;
    @FXML
    private TableColumn<ProfitPoint, String> colCostSalary;
    @FXML
    private TableColumn<ProfitPoint, String> colCostTotal;

    // ── Tab 4: Lợi nhuận ─────────────────────────────────
    @FXML
    private Label lblProfitRevenue;
    @FXML
    private Label lblProfitCost;
    @FXML
    private Label lblProfitAmount;
    @FXML
    private Label lblProfitRate;
    @FXML
    private LineChart<String, Number> profitLineChart;
    @FXML
    private TableView<ProfitPoint> tblProfit;
    @FXML
    private TableColumn<ProfitPoint, String> colProfitPeriod;
    @FXML
    private TableColumn<ProfitPoint, String> colProfitRevenue;
    @FXML
    private TableColumn<ProfitPoint, String> colProfitCost;
    @FXML
    private TableColumn<ProfitPoint, String> colProfitAmount;
    @FXML
    private TableColumn<ProfitPoint, String> colProfitRate;

    // ── Tab 5: Doanh số ───────────────────────────────────
    @FXML
    private Label lblTotalQuantity;
    @FXML
    private Label lblProductCount;
    @FXML
    private Label lblAvgQuantity;
    @FXML
    private BarChart<String, Number> salesCategoryChart;
    @FXML
    private BarChart<String, Number> topProductChart;
    @FXML
    private TableView<ProductRevenue> tblSales;
    @FXML
    private TableColumn<ProductRevenue, String> colSalesProduct;
    @FXML
    private TableColumn<ProductRevenue, String> colSalesCategory;
    @FXML
    private TableColumn<ProductRevenue, String> colSalesQuantity;

    // ─────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        initViewByCombo();
        restoreOrDefaultFilters();
        setupDateAutoFormat(fromDate);
        setupDateAutoFormat(toDate);
        hideButtonWithoutPermission();
        if (!canView)
            return;

        loadTable();
        setupListeners();
    }

    /**
     * Tự động chèn '/' sau ký tự thứ 2 (ngày) và thứ 4 (tháng).
     * Người dùng chỉ cần gõ số, định dạng dd/MM/yyyy được xây tự động.
     */
    private void setupDateAutoFormat(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null)
                return;
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 8)
                digits = digits.substring(0, 8);

            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 2 || i == 4)
                    formatted.append('/');
                formatted.append(digits.charAt(i));
            }

            String result = formatted.toString();
            if (!result.equals(newVal)) {
                field.setText(result);
                field.positionCaret(result.length());
            }
        });
    }

    private void initViewByCombo() {
        viewByCombo.setItems(FXCollections.observableArrayList(ViewBy.values()));
        viewByCombo.setValue(ViewBy.MONTH);
    }

    /** Đặt bộ lọc về mặc định (đầu tháng → hôm nay). */
    private void setupDefaultFilters() {
        fromDate.setText(LocalDate.of(2025, 12, 1).format(DATE_FMT));
        toDate.setText(LocalDate.of(2026, 2, 28).format(DATE_FMT));
    }

    /** Khôi phục bộ lọc đã lưu; nếu chưa có thì dùng giá trị mặc định. */
    private void restoreOrDefaultFilters() {
        if (savedFromDate != null && savedToDate != null) {
            fromDate.setText(savedFromDate);
            toDate.setText(savedToDate);
            if (savedViewBy != null)
                viewByCombo.setValue(savedViewBy);
        } else {
            setupDefaultFilters();
        }
    }

    /** Ghi nhớ giá trị bộ lọc hiện tại trước khi thực hiện tìm kiếm. */
    private void saveCurrentFilters() {
        savedFromDate = fromDate.getText();
        savedToDate = toDate.getText();
        savedViewBy = viewByCombo.getValue();
    }

    @Override
    public void loadTable() {
        // Tab 2: Doanh thu
        tblRevenue.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colRevPeriod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPeriod()));
        colRevAmount.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getRevenue())));
        colRevCount.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getInvoiceCount())));

        // Tab 3: Chi
        tblCost.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colCostPeriod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPeriod()));
        colCostImport.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getImportCost())));
        colCostSalary.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getSalaryCost())));
        colCostTotal.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getTotalCost())));

        // Tab 4: Lợi nhuận
        tblProfit.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colProfitPeriod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPeriod()));
        colProfitRevenue.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getRevenue())));
        colProfitCost.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getTotalCost())));
        colProfitAmount.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getProfit())));
        colProfitRate.setCellValueFactory(c -> {
            BigDecimal rev = c.getValue().getRevenue();
            BigDecimal profit = c.getValue().getProfit();
            if (rev.compareTo(BigDecimal.ZERO) == 0)
                return new SimpleStringProperty("—");
            BigDecimal rate = profit.multiply(BigDecimal.valueOf(100))
                    .divide(rev, 1, RoundingMode.HALF_UP);
            return new SimpleStringProperty(rate + "%");
        });

        // Tab 5: Doanh số
        tblSales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colSalesProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colSalesCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoryName()));
        colSalesQuantity.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getTotalQuantity())));
    }

    @Override
    public void setupListeners() {
        btnFilter.setOnAction(e -> handleFilter());
        btnRefresh.setOnAction(e -> {
            savedFromDate = null;
            savedToDate = null;
            savedViewBy = null;
            setupDefaultFilters();
            initViewByCombo();
            clearAll();
        });
        if (btnExport != null) {
            btnExport.setOnAction(e -> handleExportReport());
        }
    }

    private void handleFilter() {
        try {
            LocalDate from = parseDate(fromDate.getText(), "Ngày bắt đầu");
            LocalDate to = parseDate(toDate.getText(), "Ngày kết thúc");
            ViewBy viewBy = viewByCombo.getValue() != null ? viewByCombo.getValue() : ViewBy.MONTH;

            saveCurrentFilters();

            TaskUtil.executeSecure(null, PermissionKey.STATISTICS_VIEW,
                    () -> new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS,
                            StatisticBUS.getInstance().getAllStatistic(from, to, viewBy)),
                    result -> renderAll(result.getData()));
        } catch (IllegalArgumentException ex) {
            NotificationUtils.showErrorAlert(ex.getMessage(), "Lỗi nhập liệu");
        }
    }

    private void renderAll(StatisticDTO dto) {
        lastLoadedStatistic = dto;
        renderOverview(dto);
        renderRevenueTab(dto);
        renderCostTab(dto);
        renderProfitTab(dto);
        renderSalesTab(dto);
        renderReportTab(dto);
    }

    // ── Tab 1 ──────────────────────────────────────────────

    private void renderOverview(StatisticDTO dto) {
        lblOverviewRevenue.setText(fmt(dto.getTotalRevenue()));
        lblOverviewCost.setText(fmt(dto.getTotalCost()));
        lblOverviewImportCost.setText(fmt(dto.getTotalImportCost()));
        lblOverviewSalaryCost.setText(fmt(dto.getTotalSalaryCost()));
        lblOverviewProfit.setText(fmt(dto.getProfit()));
        lblOverviewInvoiceCount.setText(String.valueOf(dto.getTotalInvoiceCount()));

        overviewTrendChart.getData().clear();
        XYChart.Series<String, Number> seriesRev = new XYChart.Series<>();
        seriesRev.setName("Doanh thu");
        XYChart.Series<String, Number> seriesCost = new XYChart.Series<>();
        seriesCost.setName("Chi phí");
        for (ProfitPoint p : dto.getProfitTimeline()) {
            seriesRev.getData().add(new XYChart.Data<>(p.getPeriod(), toMil(p.getRevenue())));
            seriesCost.getData().add(new XYChart.Data<>(p.getPeriod(), toMil(p.getTotalCost())));
        }
        overviewTrendChart.getData().add(seriesRev);
        overviewTrendChart.getData().add(seriesCost);

        overviewCategoryChart.getData().clear();
        XYChart.Series<String, Number> seriesCat = new XYChart.Series<>();
        seriesCat.setName("Doanh thu");
        for (CategoryRevenue cr : dto.getCategoryRevenues()) {
            seriesCat.getData().add(new XYChart.Data<>(cr.getCategoryName(), toMil(cr.getRevenue())));
        }
        overviewCategoryChart.getData().add(seriesCat);
    }

    // ── Tab 2 ──────────────────────────────────────────────

    private void renderRevenueTab(StatisticDTO dto) {
        lblTotalRevenue.setText(fmt(dto.getTotalRevenue()));
        lblTotalInvoices.setText(String.valueOf(dto.getTotalInvoiceCount()));
        lblAvgInvoice.setText(fmt(dto.getAvgInvoiceAmount()));

        revenueLineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        for (RevenuePoint p : dto.getRevenueTimeline()) {
            series.getData().add(new XYChart.Data<>(p.getPeriod(), toMil(p.getRevenue())));
        }
        revenueLineChart.getData().add(series);

        revenueCategoryChart.getData().clear();
        XYChart.Series<String, Number> catSeries = new XYChart.Series<>();
        catSeries.setName("Doanh thu");
        for (CategoryRevenue cr : dto.getCategoryRevenues()) {
            catSeries.getData().add(new XYChart.Data<>(cr.getCategoryName(), toMil(cr.getRevenue())));
        }
        revenueCategoryChart.getData().add(catSeries);

        tblRevenue.setItems(FXCollections.observableArrayList(dto.getRevenueTimeline()));
    }

    // ── Tab 3 ──────────────────────────────────────────────

    private void renderCostTab(StatisticDTO dto) {
        lblTotalCost.setText(fmt(dto.getTotalCost()));
        lblImportCost.setText(fmt(dto.getTotalImportCost()));
        lblSalaryCost.setText(fmt(dto.getTotalSalaryCost()));
        lblImportCount.setText(String.valueOf(dto.getTotalImportCount()));

        costTimelineChart.getData().clear();
        XYChart.Series<String, Number> seriesImport = new XYChart.Series<>();
        seriesImport.setName("Nhập hàng");
        XYChart.Series<String, Number> seriesSalary = new XYChart.Series<>();
        seriesSalary.setName("Lương");
        for (ProfitPoint p : dto.getProfitTimeline()) {
            seriesImport.getData().add(new XYChart.Data<>(p.getPeriod(), toMil(p.getImportCost())));
            seriesSalary.getData().add(new XYChart.Data<>(p.getPeriod(), toMil(p.getSalaryCost())));
        }
        costTimelineChart.getData().add(seriesImport);
        costTimelineChart.getData().add(seriesSalary);

        supplierCostChart.getData().clear();
        XYChart.Series<String, Number> supSeries = new XYChart.Series<>();
        supSeries.setName("Chi nhập hàng");
        dto.getSupplierCosts().stream().limit(10)
                .forEach(s -> supSeries.getData().add(new XYChart.Data<>(s.getSupplierName(), toMil(s.getCost()))));
        supplierCostChart.getData().add(supSeries);

        tblCost.setItems(FXCollections.observableArrayList(dto.getProfitTimeline()));
    }

    // ── Tab 4 ──────────────────────────────────────────────

    private void renderProfitTab(StatisticDTO dto) {
        lblProfitRevenue.setText(fmt(dto.getTotalRevenue()));
        lblProfitCost.setText(fmt(dto.getTotalCost()));
        lblProfitAmount.setText(fmt(dto.getProfit()));

        BigDecimal rate = dto.getTotalRevenue().compareTo(BigDecimal.ZERO) != 0
                ? dto.getProfit().multiply(BigDecimal.valueOf(100))
                        .divide(dto.getTotalRevenue(), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        lblProfitRate.setText(rate + "%");

        profitLineChart.getData().clear();
        XYChart.Series<String, Number> seriesRev = new XYChart.Series<>();
        seriesRev.setName("Doanh thu");
        XYChart.Series<String, Number> seriesCost = new XYChart.Series<>();
        seriesCost.setName("Chi phí");
        XYChart.Series<String, Number> seriesProfit = new XYChart.Series<>();
        seriesProfit.setName("Lợi nhuận");
        for (ProfitPoint p : dto.getProfitTimeline()) {
            seriesRev.getData().add(new XYChart.Data<>(p.getPeriod(), toMil(p.getRevenue())));
            seriesCost.getData().add(new XYChart.Data<>(p.getPeriod(), toMil(p.getTotalCost())));
            seriesProfit.getData().add(new XYChart.Data<>(p.getPeriod(), toMil(p.getProfit())));
        }
        profitLineChart.getData().add(seriesRev);
        profitLineChart.getData().add(seriesCost);
        profitLineChart.getData().add(seriesProfit);

        tblProfit.setItems(FXCollections.observableArrayList(dto.getProfitTimeline()));
    }

    // ── Tab 5 ──────────────────────────────────────────────

    private void renderSalesTab(StatisticDTO dto) {
        int totalQty = dto.getProductRevenues().stream()
                .mapToInt(ProductRevenue::getTotalQuantity).sum();
        int productCount = dto.getProductRevenues().size();
        double avg = productCount > 0 ? (double) totalQty / productCount : 0;

        lblTotalQuantity.setText(String.valueOf(totalQty));
        lblProductCount.setText(String.valueOf(productCount));
        lblAvgQuantity.setText(String.format("%.1f", avg));

        // Group by category
        java.util.Map<String, Integer> catMap = new java.util.LinkedHashMap<>();
        for (ProductRevenue pr : dto.getProductRevenues()) {
            String cat = pr.getCategoryName() != null ? pr.getCategoryName() : "Khác";
            catMap.merge(cat, pr.getTotalQuantity(), Integer::sum);
        }

        salesCategoryChart.getData().clear();
        XYChart.Series<String, Number> catSeries = new XYChart.Series<>();
        catSeries.setName("Số lượng");
        catMap.forEach((cat, qty) -> catSeries.getData().add(new XYChart.Data<>(cat, qty)));
        salesCategoryChart.getData().add(catSeries);

        topProductChart.getData().clear();
        XYChart.Series<String, Number> prodSeries = new XYChart.Series<>();
        prodSeries.setName("Số lượng bán");
        dto.getProductRevenues().stream()
                .sorted((a, b) -> b.getTotalQuantity() - a.getTotalQuantity())
                .limit(10)
                .forEach(pr -> prodSeries.getData().add(
                        new XYChart.Data<>(pr.getProductName(), pr.getTotalQuantity())));
        topProductChart.getData().add(prodSeries);

        tblSales.setItems(FXCollections.observableArrayList(dto.getProductRevenues()));
    }

    // ── Tab 6 ──────────────────────────────────────────────

    private void renderReportTab(StatisticDTO dto) {
        if (lblReportRevenue != null)
            lblReportRevenue.setText(fmt(dto.getTotalRevenue()));
        if (lblReportCost != null)
            lblReportCost.setText(fmt(dto.getTotalCost()));
        if (lblReportProfit != null)
            lblReportProfit.setText(fmt(dto.getProfit()));
        if (lblReportRate != null) {
            BigDecimal rate = dto.getTotalRevenue().compareTo(BigDecimal.ZERO) != 0
                    ? dto.getProfit().multiply(BigDecimal.valueOf(100))
                            .divide(dto.getTotalRevenue(), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            lblReportRate.setText(rate + "%");
        }
    }

    // ── Report tab labels (Tab 6) ─────────────────────────
    @FXML
    private Label lblReportRevenue;
    @FXML
    private Label lblReportCost;
    @FXML
    private Label lblReportProfit;
    @FXML
    private Label lblReportRate;

    // ── Helpers ────────────────────────────────────────────

    private LocalDate parseDate(String text, String fieldName) {
        if (text == null || text.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " không được để trống.");
        try {
            return LocalDate.parse(text.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " không đúng định dạng dd/MM/yyyy.");
        }
    }

    private String fmt(BigDecimal value) {
        return ValidationUtils.getInstance().formatCurrency(value);
    }

    private double toMil(BigDecimal value) {
        if (value == null)
            return 0.0;
        return value.divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP).doubleValue();
    }

    private void clearAll() {
        lastLoadedStatistic = null;
        overviewTrendChart.getData().clear();
        overviewCategoryChart.getData().clear();
        revenueLineChart.getData().clear();
        revenueCategoryChart.getData().clear();
        costTimelineChart.getData().clear();
        supplierCostChart.getData().clear();
        profitLineChart.getData().clear();
        salesCategoryChart.getData().clear();
        topProductChart.getData().clear();

        tblRevenue.setItems(FXCollections.emptyObservableList());
        tblCost.setItems(FXCollections.emptyObservableList());
        tblProfit.setItems(FXCollections.emptyObservableList());
        tblSales.setItems(FXCollections.emptyObservableList());

        lblOverviewRevenue.setText("—");
        lblOverviewCost.setText("—");
        lblOverviewImportCost.setText("—");
        lblOverviewSalaryCost.setText("—");
        lblOverviewProfit.setText("—");
        lblOverviewInvoiceCount.setText("—");
        lblTotalRevenue.setText("—");
        lblTotalInvoices.setText("—");
        lblAvgInvoice.setText("—");
        lblTotalCost.setText("—");
        lblImportCost.setText("—");
        lblSalaryCost.setText("—");
        lblImportCount.setText("—");
        lblProfitRevenue.setText("—");
        lblProfitCost.setText("—");
        lblProfitAmount.setText("—");
        lblProfitRate.setText("—");
        lblTotalQuantity.setText("—");
        lblProductCount.setText("—");
        lblAvgQuantity.setText("—");
        if (lblReportRevenue != null)
            lblReportRevenue.setText("—");
        if (lblReportCost != null)
            lblReportCost.setText("—");
        if (lblReportProfit != null)
            lblReportProfit.setText("—");
        if (lblReportRate != null)
            lblReportRate.setText("—");
    }

    private void handleExportReport() {
        if (lastLoadedStatistic == null || lastLoadedStatistic.getProductRevenues().isEmpty()) {
            NotificationUtils.showErrorAlert("Chưa có dữ liệu để xuất. Vui lòng lọc thống kê trước.", "Thông báo");
            return;
        }

        try {
            LocalDate from = parseDate(fromDate.getText(), "Ngày bắt đầu");
            LocalDate to = parseDate(toDate.getText(), "Ngày kết thúc");
            ViewBy viewBy = viewByCombo.getValue() != null ? viewByCombo.getValue() : ViewBy.MONTH;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn nơi lưu báo cáo thống kê");
            fileChooser.setInitialFileName("ThongKe_LegoStore_Full_Statistic_Report.xlsx");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
            File selectedFile = fileChooser.showSaveDialog(btnExport.getScene().getWindow());
            if (selectedFile == null) {
                return;
            }

            ExcelService.getInstance().exportBusinessStatisticWorkbook(
                    lastLoadedStatistic,
                    from,
                    to,
                    viewBy.name(),
                    selectedFile);
            NotificationUtils.showInfoAlert("Xuất báo cáo sản phẩm thành công.", "Thông báo");
        } catch (Exception ex) {
            NotificationUtils.showErrorAlert("Xuất báo cáo thất bại: " + ex.getMessage(), "Lỗi");
        }
    }

    @Override
    public void applyFilters() {
        handleFilter();
    }

    @Override
    public void resetFilters() {
        setupDefaultFilters();
        initViewByCombo();
        clearAll();
    }

    @Override
    public void hideButtonWithoutPermission() {
        canView = SessionManagerService.getInstance().hasPermission(PermissionKey.STATISTICS_VIEW);
        if (!canView) {
            fromDate.setDisable(true);
            toDate.setDisable(true);
            viewByCombo.setDisable(true);
            btnFilter.setDisable(true);
            btnRefresh.setDisable(true);
            if (btnExport != null)
                btnExport.setDisable(true);
            clearAll();
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
        }
    }
}
