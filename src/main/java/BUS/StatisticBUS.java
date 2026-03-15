package BUS;

import DAL.StatisticDAL;
import DTO.StatisticDTO;
import DTO.StatisticDTO.CategoryRevenue;
import DTO.StatisticDTO.ImportCostPoint;
import DTO.StatisticDTO.ProfitPoint;
import DTO.StatisticDTO.RevenuePoint;
import DTO.StatisticDTO.SalaryPoint;
import DTO.StatisticDTO.SupplierCostItem;
import ENUM.Status;
import ENUM.StatusType;
import ENUM.ViewBy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class StatisticBUS {
    private static final StatisticBUS INSTANCE = new StatisticBUS();

    private StatisticBUS() {
    }

    public static StatisticBUS getInstance() {
        return INSTANCE;
    }

    // ===== VALIDATION =====

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null)
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ khoảng thời gian.");
        if (from.isAfter(to))
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc.");
        if (from.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngày bắt đầu không được ở tương lai.");
    }

    // ===== METHOD CHÍNH =====

    /**
     * Tải toàn bộ dữ liệu thống kê doanh thu–doanh số–chi phí–lợi nhuận.
     * Controller gọi phương thức này duy nhất qua TaskUtil.executeAsync.
     */
    public StatisticDTO getAllStatistic(LocalDate from, LocalDate to, ViewBy viewBy) {
        validateRange(from, to);

        ViewBy finalViewBy = (viewBy != null) ? viewBy : ViewBy.MONTH;
        int completedStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.INVOICE, Status.Invoice.COMPLETED)
                .getId();

        // --- Doanh thu ---
        List<RevenuePoint> revenueTimeline = StatisticDAL.getInstance()
                .getRevenueTimeline(from, to, finalViewBy, completedStatusId);
        List<CategoryRevenue> categoryRevenues = StatisticDAL.getInstance()
                .getCategoryRevenue(from, to, completedStatusId);
        List<StatisticDTO.ProductRevenue> productRevenues = StatisticDAL.getInstance()
                .getProductRevenue(from, to);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalInvoiceCount = 0;
        for (RevenuePoint p : revenueTimeline) {
            totalRevenue = totalRevenue.add(p.getRevenue());
            totalInvoiceCount += p.getInvoiceCount();
        }
        BigDecimal avgInvoiceAmount = totalInvoiceCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalInvoiceCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // --- Chi phí nhập hàng ---
        List<ImportCostPoint> importCostTimeline = StatisticDAL.getInstance()
                .getImportCostTimeline(from, to, finalViewBy);
        List<SupplierCostItem> supplierCosts = StatisticDAL.getInstance()
                .getImportCostBySupplier(from, to);

        BigDecimal totalImportCost = BigDecimal.ZERO;
        int totalImportCount = 0;
        for (ImportCostPoint p : importCostTimeline) {
            totalImportCost = totalImportCost.add(p.getCost());
            totalImportCount += p.getImportCount();
        }

        // --- Lương nhân viên ---
        List<SalaryPoint> salaryTimeline = StatisticDAL.getInstance()
                .getSalaryTimelineInRange(from, to);
        BigDecimal totalSalaryCost = salaryTimeline.stream()
                .map(SalaryPoint::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // --- Lợi nhuận ---
        BigDecimal totalCost = totalImportCost.add(totalSalaryCost);
        BigDecimal profit = totalRevenue.subtract(totalCost);

        List<ProfitPoint> profitTimeline = buildProfitTimeline(from, to, finalViewBy, completedStatusId);

        // --- Build DTO ---
        StatisticDTO dto = new StatisticDTO();
        dto.setRevenueTimeline(revenueTimeline);
        dto.setCategoryRevenues(categoryRevenues);
        dto.setProductRevenues(productRevenues);
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalInvoiceCount(totalInvoiceCount);
        dto.setAvgInvoiceAmount(avgInvoiceAmount);

        dto.setImportCostTimeline(importCostTimeline);
        dto.setSupplierCosts(supplierCosts);
        dto.setTotalImportCost(totalImportCost);
        dto.setTotalImportCount(totalImportCount);

        dto.setSalaryTimeline(salaryTimeline);
        dto.setTotalSalaryCost(totalSalaryCost);

        dto.setTotalCost(totalCost);
        dto.setProfit(profit);
        dto.setProfitTimeline(profitTimeline);

        return dto;
    }

    /**
     * Xây dựng timeline lợi nhuận theo tháng bằng cách gộp:
     * doanh thu (MONTH), chi nhập hàng (MONTH), và lương nhân viên (MONTH).
     */
    private List<ProfitPoint> buildProfitTimeline(LocalDate from, LocalDate to,
                                                   ViewBy viewBy, int completedStatusId) {
        List<RevenuePoint> revByMonth = StatisticDAL.getInstance()
                .getRevenueTimeline(from, to, ViewBy.MONTH, completedStatusId);
        List<ImportCostPoint> costByMonth = StatisticDAL.getInstance()
                .getImportCostTimeline(from, to, ViewBy.MONTH);
        List<SalaryPoint> salaryByMonth = StatisticDAL.getInstance()
                .getSalaryTimelineInRange(from, to);

        Map<String, BigDecimal> revMap = revByMonth.stream()
                .collect(Collectors.toMap(RevenuePoint::getPeriod, RevenuePoint::getRevenue));
        Map<String, BigDecimal> costMap = costByMonth.stream()
                .collect(Collectors.toMap(ImportCostPoint::getPeriod, ImportCostPoint::getCost));
        Map<String, BigDecimal> salaryMap = salaryByMonth.stream()
                .collect(Collectors.toMap(SalaryPoint::getPeriod, SalaryPoint::getNetSalary));

        TreeSet<String> allPeriods = new TreeSet<>();
        allPeriods.addAll(revMap.keySet());
        allPeriods.addAll(costMap.keySet());
        allPeriods.addAll(salaryMap.keySet());

        List<ProfitPoint> result = new ArrayList<>();
        for (String period : allPeriods) {
            BigDecimal rev = revMap.getOrDefault(period, BigDecimal.ZERO);
            BigDecimal importCost = costMap.getOrDefault(period, BigDecimal.ZERO);
            BigDecimal salaryCost = salaryMap.getOrDefault(period, BigDecimal.ZERO);
            BigDecimal totalCost = importCost.add(salaryCost);
            BigDecimal profit = rev.subtract(totalCost);
            result.add(new ProfitPoint(period, rev, importCost, salaryCost, totalCost, profit));
        }
        return result;
    }

    // ===== LEGACY (giữ lại để tránh break code cũ) =====

    public ArrayList<StatisticDTO.ProductRevenue> getProductRevenue(LocalDate start, LocalDate end) {
        validateRange(start, end);
        return (ArrayList<StatisticDTO.ProductRevenue>) StatisticDAL.getInstance().getProductRevenue(start, end);
    }

    public ArrayList<StatisticDTO.QuarterlyEmployeeRevenue> getQuarterlyEmployeeRevenue(int year) {
        int currentYear = LocalDate.now().getYear();
        if (year < 2000 || year > currentYear)
            throw new IllegalArgumentException("Năm thống kê không hợp lệ.");
        return (ArrayList<StatisticDTO.QuarterlyEmployeeRevenue>) StatisticDAL.getInstance()
                .getQuarterlyEmployeeRevenue(year);
    }

    public StatisticDTO getRevenueStatistic(LocalDate from, LocalDate to, ViewBy viewBy) {
        validateRange(from, to);
        ViewBy finalViewBy = (viewBy != null) ? viewBy : ViewBy.DAY;
        int completedStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.INVOICE, Status.Invoice.COMPLETED)
                .getId();
        List<RevenuePoint> timeline = StatisticDAL.getInstance()
                .getRevenueTimeline(from, to, finalViewBy, completedStatusId);
        List<CategoryRevenue> categoryRevenues = StatisticDAL.getInstance()
                .getCategoryRevenue(from, to, completedStatusId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalInvoiceCount = 0;
        for (RevenuePoint point : timeline) {
            totalRevenue = totalRevenue.add(point.getRevenue());
            totalInvoiceCount += point.getInvoiceCount();
        }
        BigDecimal avgInvoiceAmount = totalInvoiceCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalInvoiceCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        StatisticDTO dto = new StatisticDTO();
        dto.setRevenueTimeline(timeline);
        dto.setCategoryRevenues(categoryRevenues);
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalInvoiceCount(totalInvoiceCount);
        dto.setAvgInvoiceAmount(avgInvoiceAmount);
        return dto;
    }
}
