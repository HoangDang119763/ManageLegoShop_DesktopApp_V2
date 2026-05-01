package DAL;

import DTO.StatisticDTO;
import DTO.StatisticDTO.CategoryRevenue;
import DTO.StatisticDTO.RevenuePoint;
import ENUM.ViewBy;
import INTERFACE.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatisticDAL {
    public static final StatisticDAL INSTANCE = new StatisticDAL();

    private final ConnectionFactory connectionFactory;

    private StatisticDAL() {
        this.connectionFactory = ConnectApplication.getInstance().getConnectionFactory();
    }

    public static StatisticDAL getInstance() {
        return INSTANCE;
    }

    public List<StatisticDTO.QuarterlyEmployeeRevenue> getQuarterlyEmployeeRevenue(int year) {
        final String query = """
                    SELECT
                         e.id AS employee_id,
                         COALESCE(SUM(CASE WHEN QUARTER(i.created_at) = 1 THEN i.total_price - i.discount_amount ELSE 0 END), 0) AS quarter1,
                         COALESCE(SUM(CASE WHEN QUARTER(i.created_at) = 2 THEN i.total_price - i.discount_amount ELSE 0 END), 0) AS quarter2,
                         COALESCE(SUM(CASE WHEN QUARTER(i.created_at) = 3 THEN i.total_price - i.discount_amount ELSE 0 END), 0) AS quarter3,
                         COALESCE(SUM(CASE WHEN QUARTER(i.created_at) = 4 THEN i.total_price - i.discount_amount ELSE 0 END), 0) AS quarter4
                     FROM
                         employee e
                     LEFT JOIN
                         invoice i ON e.id = i.employee_id AND YEAR(i.created_at) = ?
                     GROUP BY
                         e.id;
                """;

        List<StatisticDTO.QuarterlyEmployeeRevenue> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, year);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(new StatisticDTO.QuarterlyEmployeeRevenue(
                            resultSet.getInt("employee_id"),
                            resultSet.getBigDecimal("quarter1"),
                            resultSet.getBigDecimal("quarter2"),
                            resultSet.getBigDecimal("quarter3"),
                            resultSet.getBigDecimal("quarter4")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving quarterly employee revenue statistics: " + e.getMessage());
        }
        return list;
    }

    public List<StatisticDTO.ProductRevenue> getProductRevenue(LocalDate start, LocalDate end) {
        final String query = """
                SELECT
                       p.id,
                       p.name AS product_name,
                       c.name AS category_name,
                       COALESCE(SUM(CASE WHEN i.id IS NOT NULL THEN di.quantity ELSE 0 END), 0) AS total_quantity
                   FROM
                       product p
                   JOIN
                       category c ON p.category_id = c.id
                   LEFT JOIN
                       detail_invoice di ON di.product_id = p.id
                   LEFT JOIN
                       invoice i ON di.invoice_id = i.id AND i.created_at >= ? AND i.created_at < DATE_ADD(?, INTERVAL 1 DAY)
                   GROUP BY
                       p.id, p.name, c.name
                   ORDER BY
                       total_quantity DESC;
                """;

        List<StatisticDTO.ProductRevenue> list = new ArrayList<>();
        try (Connection connection = connectionFactory.newConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDate(1, java.sql.Date.valueOf(start));
            statement.setDate(2, java.sql.Date.valueOf(end));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(new StatisticDTO.ProductRevenue(
                            resultSet.getString("id"),
                            resultSet.getString("product_name"),
                            resultSet.getString("category_name"),
                            resultSet.getInt("total_quantity")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving product revenue statistics: " + e.getMessage());
        }
        return list;
    }

    /**
     * Doanh thu theo thời gian (Module 4)
     */
    public List<RevenuePoint> getRevenueTimeline(LocalDate from, LocalDate to, ViewBy viewBy, int completedStatusId) {
        String periodExpression;
        String orderByExpression;

        switch (viewBy) {
            case MONTH:
                periodExpression = "DATE_FORMAT(i.created_at, '%Y-%m')";
                orderByExpression = "DATE_FORMAT(i.created_at, '%Y-%m')";
                break;
            case QUARTER:
                periodExpression = "CONCAT(YEAR(i.created_at), '-Q', QUARTER(i.created_at))";
                orderByExpression = "CONCAT(YEAR(i.created_at), '-Q', QUARTER(i.created_at))";
                break;
            case YEAR:
                periodExpression = "DATE_FORMAT(i.created_at, '%Y')";
                orderByExpression = "DATE_FORMAT(i.created_at, '%Y')";
                break;
            case DAY:
            default:
                periodExpression = "DATE(i.created_at)";
                orderByExpression = "DATE(i.created_at)";
                break;
        }

        final String query = """
                SELECT
                    %s AS period_label,
                    COALESCE(SUM(i.total_price - i.discount_amount), 0) AS revenue,
                    COUNT(*) AS invoice_count
                FROM invoice i
                WHERE i.created_at >= ? AND i.created_at < DATE_ADD(?, INTERVAL 1 DAY)
                  AND i.status_id = ?
                GROUP BY %s
                ORDER BY %s;
                """.formatted(periodExpression, orderByExpression, orderByExpression);

        List<RevenuePoint> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(to));
            statement.setInt(3, completedStatusId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(new RevenuePoint(
                            rs.getString("period_label"),
                            rs.getBigDecimal("revenue"),
                            rs.getInt("invoice_count")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving revenue timeline statistics: " + e.getMessage());
        }
        return list;
    }

    // ===== CHI PHÍ NHẬP HÀNG =====

    public List<StatisticDTO.ImportCostPoint> getImportCostTimeline(LocalDate from, LocalDate to, ViewBy viewBy) {
        String periodExpr;
        switch (viewBy) {
            case MONTH -> periodExpr = "DATE_FORMAT(i.created_at, '%Y-%m')";
            case QUARTER -> periodExpr = "CONCAT(YEAR(i.created_at), '-Q', QUARTER(i.created_at))";
            case YEAR  -> periodExpr = "DATE_FORMAT(i.created_at, '%Y')";
            default    -> periodExpr = "DATE(i.created_at)";
        }
        String query = """
                SELECT %s AS period_label,
                       COALESCE(SUM(i.total_price), 0) AS cost,
                       COUNT(*) AS import_count
                FROM import i
                WHERE i.created_at >= ? AND i.created_at < DATE_ADD(?, INTERVAL 1 DAY)
                GROUP BY %s
                ORDER BY %s
                """.formatted(periodExpr, periodExpr, periodExpr);

        List<StatisticDTO.ImportCostPoint> list = new ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StatisticDTO.ImportCostPoint(
                            rs.getString("period_label"),
                            rs.getBigDecimal("cost"),
                            rs.getInt("import_count")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving import cost timeline: " + e.getMessage());
        }
        return list;
    }

    public List<StatisticDTO.SupplierCostItem> getImportCostBySupplier(LocalDate from, LocalDate to) {
        String query = """
                SELECT COALESCE(s.name, 'Không rõ') AS supplier_name,
                       COALESCE(SUM(i.total_price), 0) AS cost
                FROM import i
                LEFT JOIN supplier s ON s.id = i.supplier_id
                WHERE i.created_at >= ? AND i.created_at < DATE_ADD(?, INTERVAL 1 DAY)
                GROUP BY i.supplier_id, s.name
                ORDER BY cost DESC
                LIMIT 10
                """;
        List<StatisticDTO.SupplierCostItem> list = new ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StatisticDTO.SupplierCostItem(
                            rs.getString("supplier_name"),
                            rs.getBigDecimal("cost")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving import cost by supplier: " + e.getMessage());
        }
        return list;
    }

    // ===== LƯƠNG NHÂN VIÊN (dùng cho thống kê) =====

    public List<StatisticDTO.SalaryPoint> getSalaryTimelineInRange(LocalDate from, LocalDate to) {
        return getSalaryTimelineInRange(from, to, ViewBy.MONTH);
    }

    public List<StatisticDTO.SalaryPoint> getSalaryTimelineInRange(LocalDate from, LocalDate to, ViewBy viewBy) {
        String periodExpr;
        switch (viewBy) {
            case DAY -> periodExpr = "DATE(salary_period)";
            case QUARTER -> periodExpr = "CONCAT(YEAR(salary_period), '-Q', QUARTER(salary_period))";
            case YEAR -> periodExpr = "DATE_FORMAT(salary_period, '%Y')";
            case MONTH -> periodExpr = "DATE_FORMAT(salary_period, '%Y-%m')";
            default -> periodExpr = "DATE_FORMAT(salary_period, '%Y-%m')";
        }

        String sql = """
                SELECT %s AS period_label,
                       COALESCE(SUM(net_salary), 0) AS net_salary
                FROM payroll_history
                WHERE salary_period >= ? AND salary_period <= ?
                GROUP BY %s
                ORDER BY %s
                """.formatted(periodExpr, periodExpr, periodExpr);
        List<StatisticDTO.SalaryPoint> list = new ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StatisticDTO.SalaryPoint(
                            rs.getString("period_label"),
                            rs.getBigDecimal("net_salary")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving salary timeline in range: " + e.getMessage());
        }
        return list;
    }

    public java.math.BigDecimal sumNetSalaryInRange(LocalDate from, LocalDate to) {
        String sql = """
                SELECT COALESCE(SUM(net_salary), 0) AS total_net
                FROM payroll_history
                WHERE DATE_FORMAT(salary_period, '%Y-%m') >= DATE_FORMAT(?, '%Y-%m')
                  AND DATE_FORMAT(salary_period, '%Y-%m') <= DATE_FORMAT(?, '%Y-%m')
                """;
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.math.BigDecimal val = rs.getBigDecimal("total_net");
                    return val != null ? val : java.math.BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error summing net salary in range: " + e.getMessage());
        }
        return java.math.BigDecimal.ZERO;
    }

    /**
     * Doanh thu theo danh mục sản phẩm (Module 4)
     */
    public List<CategoryRevenue> getCategoryRevenue(LocalDate from, LocalDate to, int completedStatusId) {
        final String query = """
                SELECT
                    c.name AS category_name,
                    COALESCE(SUM(di.total_price), 0) AS revenue
                FROM invoice i
                JOIN detail_invoice di ON di.invoice_id = i.id
                JOIN product p ON p.id = di.product_id
                JOIN category c ON c.id = p.category_id
                WHERE i.created_at >= ? AND i.created_at < DATE_ADD(?, INTERVAL 1 DAY)
                  AND i.status_id = ?
                GROUP BY c.id, c.name
                ORDER BY revenue DESC;
                """;

        List<CategoryRevenue> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(to));
            statement.setInt(3, completedStatusId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(new CategoryRevenue(
                            rs.getString("category_name"),
                            rs.getBigDecimal("revenue")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving category revenue statistics: " + e.getMessage());
        }
        return list;
    }
}

