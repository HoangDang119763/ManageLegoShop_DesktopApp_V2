package DTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StatisticDTO {

    // ====== 1. OVERVIEW FIELDS FOR REVENUE DASHBOARD (MODULE 4) ======
    private List<RevenuePoint> revenueTimeline = new ArrayList<>();
    private List<CategoryRevenue> categoryRevenues = new ArrayList<>();
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private int totalInvoiceCount;
    private BigDecimal avgInvoiceAmount = BigDecimal.ZERO;

    public List<RevenuePoint> getRevenueTimeline() {
        return revenueTimeline;
    }

    public void setRevenueTimeline(List<RevenuePoint> revenueTimeline) {
        this.revenueTimeline = (revenueTimeline != null) ? revenueTimeline : new ArrayList<>();
    }

    public List<CategoryRevenue> getCategoryRevenues() {
        return categoryRevenues;
    }

    public void setCategoryRevenues(List<CategoryRevenue> categoryRevenues) {
        this.categoryRevenues = (categoryRevenues != null) ? categoryRevenues : new ArrayList<>();
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = (totalRevenue != null) ? totalRevenue : BigDecimal.ZERO;
    }

    public int getTotalInvoiceCount() {
        return totalInvoiceCount;
    }

    public void setTotalInvoiceCount(int totalInvoiceCount) {
        this.totalInvoiceCount = totalInvoiceCount;
    }

    public BigDecimal getAvgInvoiceAmount() {
        return avgInvoiceAmount;
    }

    public void setAvgInvoiceAmount(BigDecimal avgInvoiceAmount) {
        this.avgInvoiceAmount = (avgInvoiceAmount != null) ? avgInvoiceAmount : BigDecimal.ZERO;
    }

    // ====== 2. INNER DTOs FOR MODULE 4 ======

    public static class RevenuePoint {
        private String period;
        private BigDecimal revenue;
        private int invoiceCount;

        public RevenuePoint(String period, BigDecimal revenue, int invoiceCount) {
            this.period = period;
            this.revenue = (revenue != null) ? revenue : BigDecimal.ZERO;
            this.invoiceCount = Math.max(0, invoiceCount);
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = (revenue != null) ? revenue : BigDecimal.ZERO;
        }

        public int getInvoiceCount() {
            return invoiceCount;
        }

        public void setInvoiceCount(int invoiceCount) {
            this.invoiceCount = Math.max(0, invoiceCount);
        }
    }

    public static class CategoryRevenue {
        private String categoryName;
        private BigDecimal revenue;

        public CategoryRevenue(String categoryName, BigDecimal revenue) {
            this.categoryName = categoryName;
            this.revenue = (revenue != null) ? revenue : BigDecimal.ZERO;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = (revenue != null) ? revenue : BigDecimal.ZERO;
        }
    }

    // ====== 3. PRODUCT SALES FIELDS (Tab doanh số) ======
    private List<ProductRevenue> productRevenues = new ArrayList<>();

    public List<ProductRevenue> getProductRevenues() { return productRevenues; }
    public void setProductRevenues(List<ProductRevenue> productRevenues) {
        this.productRevenues = (productRevenues != null) ? productRevenues : new ArrayList<>();
    }

    // ====== 4. CHI PHÍ & LỢI NHUẬN FIELDS ======
    private BigDecimal totalImportCost = BigDecimal.ZERO;
    private BigDecimal totalSalaryCost = BigDecimal.ZERO;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;
    private int totalImportCount;
    private List<ImportCostPoint> importCostTimeline = new ArrayList<>();
    private List<SupplierCostItem> supplierCosts = new ArrayList<>();
    private List<SalaryPoint> salaryTimeline = new ArrayList<>();
    private List<ProfitPoint> profitTimeline = new ArrayList<>();

    public BigDecimal getTotalImportCost() { return totalImportCost; }
    public void setTotalImportCost(BigDecimal v) { this.totalImportCost = v != null ? v : BigDecimal.ZERO; }

    public BigDecimal getTotalSalaryCost() { return totalSalaryCost; }
    public void setTotalSalaryCost(BigDecimal v) { this.totalSalaryCost = v != null ? v : BigDecimal.ZERO; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal v) { this.totalCost = v != null ? v : BigDecimal.ZERO; }

    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal v) { this.profit = v != null ? v : BigDecimal.ZERO; }

    public int getTotalImportCount() { return totalImportCount; }
    public void setTotalImportCount(int v) { this.totalImportCount = v; }

    public List<ImportCostPoint> getImportCostTimeline() { return importCostTimeline; }
    public void setImportCostTimeline(List<ImportCostPoint> v) { this.importCostTimeline = v != null ? v : new ArrayList<>(); }

    public List<SupplierCostItem> getSupplierCosts() { return supplierCosts; }
    public void setSupplierCosts(List<SupplierCostItem> v) { this.supplierCosts = v != null ? v : new ArrayList<>(); }

    public List<SalaryPoint> getSalaryTimeline() { return salaryTimeline; }
    public void setSalaryTimeline(List<SalaryPoint> v) { this.salaryTimeline = v != null ? v : new ArrayList<>(); }

    public List<ProfitPoint> getProfitTimeline() { return profitTimeline; }
    public void setProfitTimeline(List<ProfitPoint> v) { this.profitTimeline = v != null ? v : new ArrayList<>(); }

    // ====== 5. NEW INNER CLASSES ======

    public static class ImportCostPoint {
        private String period;
        private BigDecimal cost;
        private int importCount;

        public ImportCostPoint(String period, BigDecimal cost, int importCount) {
            this.period = period;
            this.cost = cost != null ? cost : BigDecimal.ZERO;
            this.importCount = importCount;
        }

        public String getPeriod() { return period; }
        public BigDecimal getCost() { return cost; }
        public int getImportCount() { return importCount; }
    }

    public static class SupplierCostItem {
        private String supplierName;
        private BigDecimal cost;

        public SupplierCostItem(String supplierName, BigDecimal cost) {
            this.supplierName = supplierName;
            this.cost = cost != null ? cost : BigDecimal.ZERO;
        }

        public String getSupplierName() { return supplierName; }
        public BigDecimal getCost() { return cost; }
    }

    public static class SalaryPoint {
        private String period;
        private BigDecimal netSalary;

        public SalaryPoint(String period, BigDecimal netSalary) {
            this.period = period;
            this.netSalary = netSalary != null ? netSalary : BigDecimal.ZERO;
        }

        public String getPeriod() { return period; }
        public BigDecimal getNetSalary() { return netSalary; }
    }

    public static class ProfitPoint {
        private String period;
        private BigDecimal revenue;
        private BigDecimal importCost;
        private BigDecimal salaryCost;
        private BigDecimal totalCost;
        private BigDecimal profit;

        public ProfitPoint(String period, BigDecimal revenue, BigDecimal importCost,
                           BigDecimal salaryCost, BigDecimal totalCost, BigDecimal profit) {
            this.period = period;
            this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
            this.importCost = importCost != null ? importCost : BigDecimal.ZERO;
            this.salaryCost = salaryCost != null ? salaryCost : BigDecimal.ZERO;
            this.totalCost = totalCost != null ? totalCost : BigDecimal.ZERO;
            this.profit = profit != null ? profit : BigDecimal.ZERO;
        }

        public String getPeriod() { return period; }
        public BigDecimal getRevenue() { return revenue; }
        public BigDecimal getImportCost() { return importCost; }
        public BigDecimal getSalaryCost() { return salaryCost; }
        public BigDecimal getTotalCost() { return totalCost; }
        public BigDecimal getProfit() { return profit; }
    }

    // ====== 6. EXISTING DTOs (giữ nguyên) ======

    public static class ProductRevenue {
        private String productId;
        private String productName;
        private String categoryName;
        private int totalQuantity;

        public ProductRevenue(String productId, String productName, String categoryName, int totalQuantity) {
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.totalQuantity = totalQuantity;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
    }

    public static class QuarterlyEmployeeRevenue {
        private int employeeId;
        private BigDecimal quarter1;
        private BigDecimal quarter2;
        private BigDecimal quarter3;
        private BigDecimal quarter4;
        private BigDecimal revenue;

        public QuarterlyEmployeeRevenue(int employeeId, BigDecimal quarter1, BigDecimal quarter2,
                                        BigDecimal quarter3, BigDecimal quarter4) {
            this.employeeId = employeeId;
            this.quarter1 = quarter1 != null ? quarter1 : BigDecimal.ZERO;
            this.quarter2 = quarter2 != null ? quarter2 : BigDecimal.ZERO;
            this.quarter3 = quarter3 != null ? quarter3 : BigDecimal.ZERO;
            this.quarter4 = quarter4 != null ? quarter4 : BigDecimal.ZERO;
            this.revenue = this.quarter1.add(this.quarter2).add(this.quarter3).add(this.quarter4);
        }

        public int getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(int employeeId) {
            this.employeeId = employeeId;
        }

        public BigDecimal getQuarter1() {
            return quarter1;
        }

        public void setQuarter1(BigDecimal quarter1) {
            this.quarter1 = quarter1;
            updateRevenue();
        }

        public BigDecimal getQuarter2() {
            return quarter2;
        }

        public void setQuarter2(BigDecimal quarter2) {
            this.quarter2 = quarter2;
            updateRevenue();
        }

        public BigDecimal getQuarter3() {
            return quarter3;
        }

        public void setQuarter3(BigDecimal quarter3) {
            this.quarter3 = quarter3;
            updateRevenue();
        }

        public BigDecimal getQuarter4() {
            return quarter4;
        }

        public void setQuarter4(BigDecimal quarter4) {
            this.quarter4 = quarter4;
            updateRevenue();
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        private void updateRevenue() {
            this.revenue = this.quarter1.add(this.quarter2).add(this.quarter3).add(this.quarter4);
        }
    }
}
