# Statistics Test Execution Report

## Scope Implemented
- Business statistics:
  - Invoice creation flow validation points.
  - Product quantity statistics (month/quarter/year verification queries).
  - Profit statistics (month/quarter/year verification queries).
  - Product report export implementation from statistics screen.
- HR statistics:
  - Data source verification queries for attendance, leave, and payroll.
  - Controller/BUS/DAL flow review coverage.

## Dataset Prepared
- Base seed data: `Data/data.sql`
- Invoice-heavy dataset: `Data/invoice_data.sql`
- Extended Jan-Feb dataset: `Data/generated_invoices_jan_feb.sql`
- Verification query pack: `Data/statistics_verification_queries.sql`

## Business Validation Checklist

### 1) Invoice creation (quantity, sell price)
- [x] `InvoiceBUS.insertFullInvoice()` enforces:
  - stock sufficiency,
  - cost snapshot on detail rows,
  - stock decrement and related transactional updates.
- [x] DB cross-check queries prepared:
  - invoice total vs detail total,
  - invalid quantity/price rows,
  - status distribution.

### 2) Product quantity by month/quarter/year
- [x] Query set prepared:
  - month: `DATE_FORMAT(..., '%Y-%m')`
  - quarter: `YEAR + QUARTER`
  - year: `YEAR(...)`
- [x] Controller supports month/year view directly (`ViewBy.MONTH`, `ViewBy.YEAR`).
- [x] Quarter support documented as DB verification path.

### 3) Profit by month/quarter/year
- [x] Formula verified in code:
  - `profit = revenue - (importCost + salaryCost)`
- [x] Query set prepared with CTEs for month/quarter/year aggregation.
- [x] Data sources mapped:
  - revenue: `invoice`
  - import cost: `import`
  - salary cost: `payroll_history`

### 4) Product report export (month/year)
- [x] Implemented export from statistics screen:
  - `src/main/java/GUI/StatisticController.java`
  - `src/main/java/SERVICE/ExcelService.java`
- [x] Export includes:
  - filter period (`fromDate`, `toDate`),
  - view mode label,
  - product id/name/category/quantity,
  - total quantity row.
- [x] Compile verification passed: `mvn -q -DskipTests compile`

## HR Statistics Validation Checklist
- [x] Screen/controller coverage reviewed:
  - `HrStatisticController`, `HrStatisticBUS`, `HrStatisticUI.fxml`
- [x] Verification queries prepared for:
  - attendance totals,
  - leave request distribution,
  - payroll totals.
- [x] Noted current data caveat in seed:
  - `payroll_history` has very limited rows, so KPI trends may look sparse.

## Observed Gaps / Risks
- HR export button still shows placeholder message (not implemented yet).
- Quarter view is not exposed directly on business statistics UI control; quarter is currently verification via DB aggregation.
- Seed payroll data volume is small for robust trend testing.

## Next Recommended Execution Steps
1. Import one selected dataset (`data.sql` + optional invoice augmentation).
2. Run all queries in `Data/statistics_verification_queries.sql`.
3. Open app and execute UI checks for corresponding periods.
4. Export product report from statistics tab and compare with DB query outputs.
