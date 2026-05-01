-- =========================================================
-- Statistics verification queries (Business + HR)
-- Scope:
-- 1) Invoice creation integrity
-- 2) Product quantity statistics (month/quarter/year)
-- 3) Profit statistics (month/quarter/year)
-- 4) HR statistics data source checks
-- =========================================================

-- ---------------------------------------------------------
-- [A] Invoice creation integrity
-- ---------------------------------------------------------

-- A1. Verify invoice master and detail consistency
SELECT i.id,
       i.created_at,
       i.total_price AS invoice_total,
       COALESCE(SUM(di.total_price), 0) AS detail_total,
       (i.total_price - COALESCE(SUM(di.total_price), 0)) AS diff
FROM invoice i
LEFT JOIN detail_invoice di ON di.invoice_id = i.id
GROUP BY i.id, i.created_at, i.total_price
HAVING ABS(i.total_price - COALESCE(SUM(di.total_price), 0)) > 0.01
ORDER BY i.id DESC;

-- A2. Verify sold quantity > 0 and selling price >= 0
SELECT di.invoice_id, di.product_id, di.quantity, di.price, di.total_price
FROM detail_invoice di
WHERE di.quantity <= 0 OR di.price < 0 OR di.total_price < 0;

-- A3. Check invoice status distribution (Completed is required for statistics)
SELECT s.type, s.status_name, s.description, COUNT(*) AS total
FROM invoice i
JOIN status s ON s.id = i.status_id
GROUP BY s.type, s.status_name, s.description
ORDER BY total DESC;

-- ---------------------------------------------------------
-- [B] Product quantity statistics
-- ---------------------------------------------------------

-- B1. Month quantity
SELECT DATE_FORMAT(i.created_at, '%Y-%m') AS period_month,
       SUM(di.quantity) AS total_quantity
FROM invoice i
JOIN detail_invoice di ON di.invoice_id = i.id
WHERE i.status_id = (
    SELECT id FROM status
    WHERE type = 'INVOICE' AND status_name = 'COMPLETED'
    LIMIT 1
)
GROUP BY DATE_FORMAT(i.created_at, '%Y-%m')
ORDER BY period_month;

-- B2. Quarter quantity
SELECT CONCAT(YEAR(i.created_at), '-Q', QUARTER(i.created_at)) AS period_quarter,
       SUM(di.quantity) AS total_quantity
FROM invoice i
JOIN detail_invoice di ON di.invoice_id = i.id
WHERE i.status_id = (
    SELECT id FROM status
    WHERE type = 'INVOICE' AND status_name = 'COMPLETED'
    LIMIT 1
)
GROUP BY YEAR(i.created_at), QUARTER(i.created_at)
ORDER BY YEAR(i.created_at), QUARTER(i.created_at);

-- B3. Year quantity
SELECT YEAR(i.created_at) AS period_year,
       SUM(di.quantity) AS total_quantity
FROM invoice i
JOIN detail_invoice di ON di.invoice_id = i.id
WHERE i.status_id = (
    SELECT id FROM status
    WHERE type = 'INVOICE' AND status_name = 'COMPLETED'
    LIMIT 1
)
GROUP BY YEAR(i.created_at)
ORDER BY period_year;

-- ---------------------------------------------------------
-- [C] Profit statistics
-- Profit = Revenue - (ImportCost + SalaryCost)
-- ---------------------------------------------------------

-- C1. Revenue by month
WITH revenue_month AS (
    SELECT DATE_FORMAT(i.created_at, '%Y-%m') AS period,
           SUM(i.total_price - i.discount_amount) AS revenue
    FROM invoice i
    WHERE i.status_id = (
        SELECT id FROM status
        WHERE type = 'INVOICE' AND status_name = 'COMPLETED'
        LIMIT 1
    )
    GROUP BY DATE_FORMAT(i.created_at, '%Y-%m')
),
import_month AS (
    SELECT DATE_FORMAT(im.created_at, '%Y-%m') AS period,
           SUM(im.total_price) AS import_cost
    FROM import im
    GROUP BY DATE_FORMAT(im.created_at, '%Y-%m')
),
salary_month AS (
    SELECT DATE_FORMAT(ph.salary_period, '%Y-%m') AS period,
           SUM(ph.net_salary) AS salary_cost
    FROM payroll_history ph
    GROUP BY DATE_FORMAT(ph.salary_period, '%Y-%m')
)
SELECT p.period,
       COALESCE(r.revenue, 0) AS revenue,
       COALESCE(im.import_cost, 0) AS import_cost,
       COALESCE(sm.salary_cost, 0) AS salary_cost,
       (COALESCE(im.import_cost, 0) + COALESCE(sm.salary_cost, 0)) AS total_cost,
       (COALESCE(r.revenue, 0) - (COALESCE(im.import_cost, 0) + COALESCE(sm.salary_cost, 0))) AS profit
FROM (
    SELECT period FROM revenue_month
    UNION
    SELECT period FROM import_month
    UNION
    SELECT period FROM salary_month
) p
LEFT JOIN revenue_month r ON r.period = p.period
LEFT JOIN import_month im ON im.period = p.period
LEFT JOIN salary_month sm ON sm.period = p.period
ORDER BY p.period;

-- C2. Quarter profit
WITH revenue_quarter AS (
    SELECT CONCAT(YEAR(i.created_at), '-Q', QUARTER(i.created_at)) AS period,
           SUM(i.total_price - i.discount_amount) AS revenue
    FROM invoice i
    WHERE i.status_id = (
        SELECT id FROM status
        WHERE type = 'INVOICE' AND status_name = 'COMPLETED'
        LIMIT 1
    )
    GROUP BY YEAR(i.created_at), QUARTER(i.created_at)
),
import_quarter AS (
    SELECT CONCAT(YEAR(im.created_at), '-Q', QUARTER(im.created_at)) AS period,
           SUM(im.total_price) AS import_cost
    FROM import im
    GROUP BY YEAR(im.created_at), QUARTER(im.created_at)
),
salary_quarter AS (
    SELECT CONCAT(YEAR(ph.salary_period), '-Q', QUARTER(ph.salary_period)) AS period,
           SUM(ph.net_salary) AS salary_cost
    FROM payroll_history ph
    GROUP BY YEAR(ph.salary_period), QUARTER(ph.salary_period)
)
SELECT p.period,
       COALESCE(r.revenue, 0) AS revenue,
       COALESCE(im.import_cost, 0) AS import_cost,
       COALESCE(sm.salary_cost, 0) AS salary_cost,
       (COALESCE(r.revenue, 0) - (COALESCE(im.import_cost, 0) + COALESCE(sm.salary_cost, 0))) AS profit
FROM (
    SELECT period FROM revenue_quarter
    UNION
    SELECT period FROM import_quarter
    UNION
    SELECT period FROM salary_quarter
) p
LEFT JOIN revenue_quarter r ON r.period = p.period
LEFT JOIN import_quarter im ON im.period = p.period
LEFT JOIN salary_quarter sm ON sm.period = p.period
ORDER BY p.period;

-- C3. Year profit
WITH revenue_year AS (
    SELECT YEAR(i.created_at) AS period,
           SUM(i.total_price - i.discount_amount) AS revenue
    FROM invoice i
    WHERE i.status_id = (
        SELECT id FROM status
        WHERE type = 'INVOICE' AND status_name = 'COMPLETED'
        LIMIT 1
    )
    GROUP BY YEAR(i.created_at)
),
import_year AS (
    SELECT YEAR(im.created_at) AS period,
           SUM(im.total_price) AS import_cost
    FROM import im
    GROUP BY YEAR(im.created_at)
),
salary_year AS (
    SELECT YEAR(ph.salary_period) AS period,
           SUM(ph.net_salary) AS salary_cost
    FROM payroll_history ph
    GROUP BY YEAR(ph.salary_period)
)
SELECT p.period,
       COALESCE(r.revenue, 0) AS revenue,
       COALESCE(im.import_cost, 0) AS import_cost,
       COALESCE(sm.salary_cost, 0) AS salary_cost,
       (COALESCE(r.revenue, 0) - (COALESCE(im.import_cost, 0) + COALESCE(sm.salary_cost, 0))) AS profit
FROM (
    SELECT period FROM revenue_year
    UNION
    SELECT period FROM import_year
    UNION
    SELECT period FROM salary_year
) p
LEFT JOIN revenue_year r ON r.period = p.period
LEFT JOIN import_year im ON im.period = p.period
LEFT JOIN salary_year sm ON sm.period = p.period
ORDER BY p.period;

-- ---------------------------------------------------------
-- [D] HR statistics source checks
-- ---------------------------------------------------------

-- D1. Attendance records by month
SELECT DATE_FORMAT(ts.check_in, '%Y-%m') AS period_month,
       COUNT(*) AS total_sessions,
       COUNT(DISTINCT ts.employee_id) AS employee_count,
       SUM(ts.work_hours) AS total_work_hours,
       SUM(ts.ot_hours) AS total_ot_hours
FROM time_sheet ts
GROUP BY DATE_FORMAT(ts.check_in, '%Y-%m')
ORDER BY period_month;

-- D2. Leave requests by month and status
SELECT DATE_FORMAT(lr.start_date, '%Y-%m') AS period_month,
       s.description AS status_desc,
       COUNT(*) AS total_requests
FROM leave_request lr
LEFT JOIN status s ON s.id = lr.status_id
GROUP BY DATE_FORMAT(lr.start_date, '%Y-%m'), s.description
ORDER BY period_month, status_desc;

-- D3. Payroll (HR salary tab source)
SELECT DATE_FORMAT(ph.salary_period, '%Y-%m') AS period_month,
       COUNT(*) AS employee_rows,
       SUM(ph.net_salary) AS total_net_salary,
       AVG(ph.net_salary) AS avg_net_salary
FROM payroll_history ph
GROUP BY DATE_FORMAT(ph.salary_period, '%Y-%m')
ORDER BY period_month;
