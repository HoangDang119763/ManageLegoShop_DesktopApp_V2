package BUS;

import DAL.EmployeeDAL;
import DAL.EmploymentHistoryDAL;
import DAL.LeaveRequestDAL;
import DAL.PayrollHistoryDAL;
import DAL.TimeSheetDAL;
import DTO.AllowanceDTO;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.FineDTO;
import DTO.HrStatisticDTO;
import DTO.HrStatisticDTO.DepartmentDistributionItem;
import DTO.HrStatisticDTO.FineRewardRow;
import DTO.HrStatisticDTO.HeadcountPoint;
import DTO.HrStatisticDTO.RewardFineSummary;
import DTO.HrStatisticDTO.StatusDistributionItem;
import DTO.PositionDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HrStatisticBUS {

        private static final HrStatisticBUS INSTANCE = new HrStatisticBUS();
        private static final int MEAL_ALLOWANCE_ID = 1;
        private static final int ACCOMMODATION_ALLOWANCE_ID = 2;
        private static final int TRANSPORT_ALLOWANCE_ID = 3;

        private HrStatisticBUS() {
        }

        public static HrStatisticBUS getInstance() {
                return INSTANCE;
        }

        public HrStatisticDTO getHrStatistic(int month, int year) {
                if (month < 1 || month > 12) {
                        throw new IllegalArgumentException("Tháng không hợp lệ (1-12).");
                }
                int currentYear = LocalDate.now().getYear();
                if (year < 2000 || year > currentYear) {
                        throw new IllegalArgumentException("Năm thống kê không hợp lệ.");
                }

                int totalEmployees = EmployeeDAL.getInstance().countActiveEmployees();
                int newEmployees = EmployeeDAL.getInstance().countNewEmployeesInMonth(month, year);
                BigDecimal totalPaidSalary = PayrollHistoryDAL.getInstance().sumNetSalaryByMonthYear(month, year);
                List<HeadcountPoint> headcountOverTime = EmploymentHistoryDAL.getInstance().getHeadcountOverTime(month,
                                year);
                List<StatusDistributionItem> statusDistribution = EmployeeDAL.getInstance().getStatusDistribution();
                List<DepartmentDistributionItem> departmentDistribution = EmployeeDAL.getInstance()
                                .getDepartmentDistribution();

                HrStatisticDTO dto = new HrStatisticDTO();
                dto.setTotalEmployees(totalEmployees);
                dto.setNewEmployees(newEmployees);
                dto.setTotalPaidSalary(totalPaidSalary);
                dto.setHeadcountOverTime(headcountOverTime);
                dto.setHeadcountChanges(EmploymentHistoryDAL.getInstance().getHeadcountChanges(month, year));
                dto.setStatusDistribution(statusDistribution);
                dto.setDepartmentDistribution(departmentDistribution);
                dto.setRewardFineSummary(buildRewardFineSummary(month, year));
                dto.setFineRewardRows(buildFineRewardRows(month, year));

                // Chấm công
                dto.setAttendanceStat(TimeSheetDAL.getInstance().getAttendanceStat(month, year));
                dto.setAttendanceRows(TimeSheetDAL.getInstance().getAttendanceRows(month, year));
                dto.setDailyWorkPoints(TimeSheetDAL.getInstance().getDailyWorkPoints(month, year));

                // Nghỉ phép
                dto.setLeaveStat(LeaveRequestDAL.getInstance().getLeaveStat(month, year));
                dto.setLeaveByType(LeaveRequestDAL.getInstance().getLeaveByType(month, year));
                dto.setLeaveByStatus(LeaveRequestDAL.getInstance().getLeaveByStatus(month, year));
                dto.setLeaveRows(LeaveRequestDAL.getInstance().getLeaveRows(month, year));

                // Thống kê lương
                dto.setSalaryStat(PayrollHistoryDAL.getInstance().getSalaryStatForMonth(month, year));
                dto.setSalaryRows(PayrollHistoryDAL.getInstance().getSalaryRows(month, year));

                return dto;
        }

        private RewardFineSummary buildRewardFineSummary(int month, int year) {
                List<FineDTO> finesInMonth = FineBUS.getInstance().getAll().stream()
                                .filter(f -> f.getCreatedAt() != null
                                                && f.getCreatedAt().getYear() == year
                                                && f.getCreatedAt().getMonthValue() == month)
                                .collect(Collectors.toList());
                Map<Integer, BigDecimal> allowanceAmountById = getAllowanceAmountById();
                List<EmployeeDTO> employees = EmployeeDAL.getInstance().getAll();

                BigDecimal totalAllowance = employees.stream()
                                .map(e -> calculateEmployeeAllowance(e, allowanceAmountById))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                long employeesWithAllowance = employees.stream()
                                .filter(this::hasAnyAllowanceSupport)
                                .count();

                BigDecimal totalReward = finesInMonth.stream()
                                .filter(f -> "REWARD".equalsIgnoreCase(f.getType()))
                                .map(f -> safeAmount(f.getAmount()).abs())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                long employeesWithReward = finesInMonth.stream()
                                .filter(f -> "REWARD".equalsIgnoreCase(f.getType()))
                                .map(FineDTO::getEmployeeId)
                                .distinct()
                                .count();

                BigDecimal totalFine = finesInMonth.stream()
                                .filter(f -> "DISCIPLINE".equalsIgnoreCase(f.getType()))
                                .map(f -> safeAmount(f.getAmount()).abs())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                long employeesWithFine = finesInMonth.stream()
                                .filter(f -> "DISCIPLINE".equalsIgnoreCase(f.getType()))
                                .map(FineDTO::getEmployeeId)
                                .distinct()
                                .count();

                RewardFineSummary summary = new RewardFineSummary();
                summary.setTotalAllowance(totalAllowance);
                summary.setEmployeesWithAllowance((int) employeesWithAllowance);
                summary.setTotalReward(totalReward);
                summary.setEmployeesWithReward((int) employeesWithReward);
                summary.setTotalFine(totalFine);
                summary.setEmployeesWithFine((int) employeesWithFine);
                return summary;
        }

        private List<FineRewardRow> buildFineRewardRows(int month, int year) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                List<FineDTO> finesInMonth = FineBUS.getInstance().getAll().stream()
                                .filter(f -> f.getCreatedAt() != null
                                                && f.getCreatedAt().getYear() == year
                                                && f.getCreatedAt().getMonthValue() == month)
                                .collect(Collectors.toList());

                Map<Integer, EmployeeDTO> empMap = EmployeeDAL.getInstance().getAll()
                                .stream().collect(Collectors.toMap(EmployeeDTO::getId, e -> e,
                                                (a, b) -> a));

                Map<Integer, String> deptMap = DepartmentBUS.getInstance().getAll()
                                .stream().collect(Collectors.toMap(DepartmentDTO::getId, DepartmentDTO::getName,
                                                (a, b) -> a));

                Map<Integer, String> posMap = PositionBUS.getInstance().getAll()
                                .stream().collect(Collectors.toMap(PositionDTO::getId, PositionDTO::getName,
                                                (a, b) -> a));

                Map<Integer, BigDecimal> allowanceAmountById = getAllowanceAmountById();
                List<FineRewardRow> rows = new ArrayList<>();

                EmployeeDAL.getInstance().getAll().stream()
                                .filter(this::hasAnyAllowanceSupport)
                                .forEach(emp -> {
                                        BigDecimal allowanceAmount = calculateEmployeeAllowance(emp, allowanceAmountById);
                                        if (allowanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                                                return;
                                        }
                                        rows.add(new FineRewardRow(
                                                        formatEmployeeCode(emp),
                                                        emp.getFullName().trim(),
                                                        emp.getDepartmentId() != null
                                                                        ? deptMap.getOrDefault(emp.getDepartmentId(), "—")
                                                                        : "—",
                                                        emp.getPositionId() != null
                                                                        ? posMap.getOrDefault(emp.getPositionId(), "—")
                                                                        : "—",
                                                        "Phụ cấp",
                                                        "ALLOWANCE",
                                                        allowanceAmount,
                                                        "Hiện tại"));
                                });

                rows.addAll(finesInMonth.stream().map(f -> {
                        EmployeeDTO emp = empMap.get(f.getEmployeeId());
                        String code = emp != null ? formatEmployeeCode(emp) : "—";
                        String name = emp != null
                                        ? emp.getFullName().trim()
                                        : "—";
                        String dept = (emp != null && emp.getDepartmentId() != null)
                                        ? deptMap.getOrDefault(emp.getDepartmentId(), "—")
                                        : "—";
                        String pos = (emp != null && emp.getPositionId() != null)
                                        ? posMap.getOrDefault(emp.getPositionId(), "—")
                                        : "—";
                        String date = f.getCreatedAt() != null
                                        ? f.getCreatedAt().format(fmt)
                                        : "—";
                        String type = f.getType() != null ? f.getType() : "DISCIPLINE";
                        String typeLabel = "REWARD".equalsIgnoreCase(type) ? "Khen thưởng" : "Vi phạm";
                        String level = f.getFineLevel() != null && !f.getFineLevel().isBlank()
                                        ? typeLabel + " - " + f.getFineLevel()
                                        : typeLabel;
                        return new FineRewardRow(code, name, dept, pos,
                                        level,
                                        type.toUpperCase(),
                                        safeAmount(f.getAmount()).abs(), date);
                }).collect(Collectors.toList()));

                return rows;
        }

        private Map<Integer, BigDecimal> getAllowanceAmountById() {
                return AllowanceBUS.getInstance().getAll().stream()
                                .collect(Collectors.toMap(AllowanceDTO::getId,
                                                a -> safeAmount(a.getAmount()),
                                                (a, b) -> a));
        }

        private boolean hasAnyAllowanceSupport(EmployeeDTO employee) {
                return employee != null && (employee.isMealSupport()
                                || employee.isAccommodationSupport()
                                || employee.isTransportationSupport());
        }

        private BigDecimal calculateEmployeeAllowance(EmployeeDTO employee, Map<Integer, BigDecimal> allowanceAmountById) {
                if (employee == null) {
                        return BigDecimal.ZERO;
                }
                BigDecimal total = BigDecimal.ZERO;
                if (employee.isMealSupport()) {
                        total = total.add(allowanceAmountById.getOrDefault(MEAL_ALLOWANCE_ID, BigDecimal.ZERO));
                }
                if (employee.isAccommodationSupport()) {
                        total = total.add(allowanceAmountById.getOrDefault(ACCOMMODATION_ALLOWANCE_ID, BigDecimal.ZERO));
                }
                if (employee.isTransportationSupport()) {
                        total = total.add(allowanceAmountById.getOrDefault(TRANSPORT_ALLOWANCE_ID, BigDecimal.ZERO));
                }
                return total;
        }

        private BigDecimal safeAmount(BigDecimal value) {
                return value != null ? value : BigDecimal.ZERO;
        }

        private String formatEmployeeCode(EmployeeDTO employee) {
                return employee != null ? String.format("NV%05d", employee.getId()) : "—";
        }
}
