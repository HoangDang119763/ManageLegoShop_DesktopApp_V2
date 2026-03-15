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
                // dto.setLeaveStat(LeaveRequestDAL.getInstance().getLeaveStat(month, year));
                // dto.setLeaveByType(LeaveRequestDAL.getInstance().getLeaveByType(month,
                // year));
                // dto.setLeaveByStatus(LeaveRequestDAL.getInstance().getLeaveByStatus(month,
                // year));
                // dto.setLeaveRows(LeaveRequestDAL.getInstance().getLeaveRows(month, year));

                // Thống kê lương
                dto.setSalaryStat(PayrollHistoryDAL.getInstance().getSalaryStatForMonth(month, year));
                dto.setSalaryRows(PayrollHistoryDAL.getInstance().getSalaryRows(month, year));

                return dto;
        }

        private RewardFineSummary buildRewardFineSummary(int month, int year) {
                ArrayList<AllowanceDTO> allAllowances = AllowanceBUS.getInstance().getAll();
                BigDecimal totalAllowance = allAllowances.stream()
                                .map(a -> a.getAmount() != null ? a.getAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<FineDTO> finesInMonth = FineBUS.getInstance().getAll().stream()
                                .filter(f -> f.getCreatedAt() != null
                                                && f.getCreatedAt().getYear() == year
                                                && f.getCreatedAt().getMonthValue() == month)
                                .collect(Collectors.toList());
                BigDecimal totalFine = finesInMonth.stream()
                                .map(f -> f.getAmount() != null ? f.getAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                long employeesWithFine = finesInMonth.stream()
                                .map(FineDTO::getEmployeeId)
                                .distinct()
                                .count();

                RewardFineSummary summary = new RewardFineSummary();
                summary.setTotalAllowance(totalAllowance);
                summary.setEmployeesWithAllowance(allAllowances.size());
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

                return finesInMonth.stream().map(f -> {
                        EmployeeDTO emp = empMap.get(f.getEmployeeId());
                        String code = emp != null ? String.format("NV%05d", emp.getId()) : "—";
                        String name = emp != null
                                        ? (emp.getFirstName() + " " + emp.getLastName()).trim()
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
                        return new FineRewardRow(code, name, dept, pos,
                                        f.getFineLevel() != null ? f.getFineLevel() : "—",
                                        f.getAmount(), date);
                }).collect(Collectors.toList());
        }
}
