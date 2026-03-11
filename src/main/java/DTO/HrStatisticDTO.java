package DTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HrStatisticDTO {
    private int totalEmployees;
    private int newEmployees;
    private BigDecimal totalPaidSalary = BigDecimal.ZERO;
    private List<HeadcountPoint> headcountOverTime = new ArrayList<>();
    private List<StatusDistributionItem> statusDistribution = new ArrayList<>();
    private List<DepartmentDistributionItem> departmentDistribution = new ArrayList<>();
    private RewardFineSummary rewardFineSummary = new RewardFineSummary();
    private List<FineRewardRow> fineRewardRows = new ArrayList<>();

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(int totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public int getNewEmployees() {
        return newEmployees;
    }

    public void setNewEmployees(int newEmployees) {
        this.newEmployees = newEmployees;
    }

    public BigDecimal getTotalPaidSalary() {
        return totalPaidSalary;
    }

    public void setTotalPaidSalary(BigDecimal totalPaidSalary) {
        this.totalPaidSalary = totalPaidSalary != null ? totalPaidSalary : BigDecimal.ZERO;
    }

    public List<HeadcountPoint> getHeadcountOverTime() {
        return headcountOverTime;
    }

    public void setHeadcountOverTime(List<HeadcountPoint> headcountOverTime) {
        this.headcountOverTime = headcountOverTime != null ? headcountOverTime : new ArrayList<>();
    }

    public List<StatusDistributionItem> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(List<StatusDistributionItem> statusDistribution) {
        this.statusDistribution = statusDistribution != null ? statusDistribution : new ArrayList<>();
    }

    public List<DepartmentDistributionItem> getDepartmentDistribution() {
        return departmentDistribution;
    }

    public void setDepartmentDistribution(List<DepartmentDistributionItem> departmentDistribution) {
        this.departmentDistribution = departmentDistribution != null ? departmentDistribution : new ArrayList<>();
    }

    public RewardFineSummary getRewardFineSummary() {
        return rewardFineSummary;
    }

    public void setRewardFineSummary(RewardFineSummary rewardFineSummary) {
        this.rewardFineSummary = rewardFineSummary != null ? rewardFineSummary : new RewardFineSummary();
    }

    public List<FineRewardRow> getFineRewardRows() {
        return fineRewardRows;
    }

    public void setFineRewardRows(List<FineRewardRow> fineRewardRows) {
        this.fineRewardRows = fineRewardRows != null ? fineRewardRows : new ArrayList<>();
    }

    // ===== Inner DTOs =====

    public static class HeadcountPoint {
        private String label;
        private int headcount;

        public HeadcountPoint(String label, int headcount) {
            this.label = label;
            this.headcount = headcount;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getHeadcount() {
            return headcount;
        }

        public void setHeadcount(int headcount) {
            this.headcount = headcount;
        }
    }

    public static class StatusDistributionItem {
        private String statusName;
        private int count;

        public StatusDistributionItem(String statusName, int count) {
            this.statusName = statusName;
            this.count = count;
        }

        public String getStatusName() {
            return statusName;
        }

        public void setStatusName(String statusName) {
            this.statusName = statusName;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class DepartmentDistributionItem {
        private String departmentName;
        private int count;

        public DepartmentDistributionItem(String departmentName, int count) {
            this.departmentName = departmentName;
            this.count = count;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class FineRewardRow {
        private String employeeCode;
        private String fullName;
        private String departmentName;
        private String positionName;
        private String fineLevel;
        private BigDecimal amount;
        private String createdAt;

        public FineRewardRow(String employeeCode, String fullName, String departmentName,
                             String positionName, String fineLevel,
                             BigDecimal amount, String createdAt) {
            this.employeeCode = employeeCode;
            this.fullName = fullName;
            this.departmentName = departmentName;
            this.positionName = positionName;
            this.fineLevel = fineLevel;
            this.amount = amount != null ? amount : BigDecimal.ZERO;
            this.createdAt = createdAt;
        }

        public String getEmployeeCode() { return employeeCode; }
        public String getFullName() { return fullName; }
        public String getDepartmentName() { return departmentName; }
        public String getPositionName() { return positionName; }
        public String getFineLevel() { return fineLevel; }
        public BigDecimal getAmount() { return amount; }
        public String getCreatedAt() { return createdAt; }
    }

    // ===== Thay đổi nhân sự (Employment History) =====

    private List<HeadcountChangeRow> headcountChanges = new ArrayList<>();

    public List<HeadcountChangeRow> getHeadcountChanges() { return headcountChanges; }
    public void setHeadcountChanges(List<HeadcountChangeRow> v) {
        this.headcountChanges = v != null ? v : new ArrayList<>();
    }

    public static class HeadcountChangeRow {
        private final String fullName;
        private final String fromDept;
        private final String toDept;
        private final String fromPos;
        private final String toPos;
        private final String effectiveDate;
        private final String status;

        public HeadcountChangeRow(String fullName, String fromDept, String toDept,
                                  String fromPos, String toPos,
                                  String effectiveDate, String status) {
            this.fullName = fullName;
            this.fromDept = fromDept;
            this.toDept = toDept;
            this.fromPos = fromPos;
            this.toPos = toPos;
            this.effectiveDate = effectiveDate;
            this.status = status;
        }

        public String getFullName() { return fullName; }
        public String getFromDept() { return fromDept; }
        public String getToDept() { return toDept; }
        public String getFromPos() { return fromPos; }
        public String getToPos() { return toPos; }
        public String getEffectiveDate() { return effectiveDate; }
        public String getStatus() { return status; }
    }

    // ===== Chấm công =====

    private AttendanceStat attendanceStat = new AttendanceStat();
    private List<AttendanceRow> attendanceRows = new ArrayList<>();
    private List<DailyWorkPoint> dailyWorkPoints = new ArrayList<>();

    public AttendanceStat getAttendanceStat() { return attendanceStat; }
    public void setAttendanceStat(AttendanceStat v) { this.attendanceStat = v != null ? v : new AttendanceStat(); }
    public List<AttendanceRow> getAttendanceRows() { return attendanceRows; }
    public void setAttendanceRows(List<AttendanceRow> v) { this.attendanceRows = v != null ? v : new ArrayList<>(); }
    public List<DailyWorkPoint> getDailyWorkPoints() { return dailyWorkPoints; }
    public void setDailyWorkPoints(List<DailyWorkPoint> v) { this.dailyWorkPoints = v != null ? v : new ArrayList<>(); }

    // ===== Nghỉ phép =====

    private LeaveStat leaveStat = new LeaveStat();
    private List<LeaveByTypeItem> leaveByType = new ArrayList<>();
    private List<LeaveStatusItem> leaveByStatus = new ArrayList<>();
    private List<LeaveRow> leaveRows = new ArrayList<>();

    public LeaveStat getLeaveStat() { return leaveStat; }
    public void setLeaveStat(LeaveStat v) { this.leaveStat = v != null ? v : new LeaveStat(); }
    public List<LeaveByTypeItem> getLeaveByType() { return leaveByType; }
    public void setLeaveByType(List<LeaveByTypeItem> v) { this.leaveByType = v != null ? v : new ArrayList<>(); }
    public List<LeaveStatusItem> getLeaveByStatus() { return leaveByStatus; }
    public void setLeaveByStatus(List<LeaveStatusItem> v) { this.leaveByStatus = v != null ? v : new ArrayList<>(); }
    public List<LeaveRow> getLeaveRows() { return leaveRows; }
    public void setLeaveRows(List<LeaveRow> v) { this.leaveRows = v != null ? v : new ArrayList<>(); }

    // ===== Thống kê lương =====

    private SalaryStat salaryStat = new SalaryStat();
    private List<SalaryRow> salaryRows = new ArrayList<>();

    public SalaryStat getSalaryStat() { return salaryStat; }
    public void setSalaryStat(SalaryStat v) { this.salaryStat = v != null ? v : new SalaryStat(); }
    public List<SalaryRow> getSalaryRows() { return salaryRows; }
    public void setSalaryRows(List<SalaryRow> v) { this.salaryRows = v != null ? v : new ArrayList<>(); }

    // ===== New Inner DTOs =====

    public static class AttendanceStat {
        private int totalSessions;
        private int employeeCount;
        private java.math.BigDecimal totalWorkHours = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal totalOtHours = java.math.BigDecimal.ZERO;

        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int v) { this.totalSessions = v; }
        public int getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(int v) { this.employeeCount = v; }
        public java.math.BigDecimal getTotalWorkHours() { return totalWorkHours; }
        public void setTotalWorkHours(java.math.BigDecimal v) { this.totalWorkHours = v != null ? v : java.math.BigDecimal.ZERO; }
        public java.math.BigDecimal getTotalOtHours() { return totalOtHours; }
        public void setTotalOtHours(java.math.BigDecimal v) { this.totalOtHours = v != null ? v : java.math.BigDecimal.ZERO; }
    }

    public static class AttendanceRow {
        private String fullName;
        private String deptName;
        private int sessionCount;
        private java.math.BigDecimal totalWork;
        private java.math.BigDecimal totalOt;

        public AttendanceRow(String fullName, String deptName, int sessionCount,
                             java.math.BigDecimal totalWork, java.math.BigDecimal totalOt) {
            this.fullName = fullName;
            this.deptName = deptName;
            this.sessionCount = sessionCount;
            this.totalWork = totalWork != null ? totalWork : java.math.BigDecimal.ZERO;
            this.totalOt = totalOt != null ? totalOt : java.math.BigDecimal.ZERO;
        }

        public String getFullName() { return fullName; }
        public String getDeptName() { return deptName; }
        public int getSessionCount() { return sessionCount; }
        public java.math.BigDecimal getTotalWork() { return totalWork; }
        public java.math.BigDecimal getTotalOt() { return totalOt; }
    }

    public static class DailyWorkPoint {
        private String dayLabel;
        private java.math.BigDecimal totalWork;
        private int sessionCount;

        public DailyWorkPoint(String dayLabel, java.math.BigDecimal totalWork, int sessionCount) {
            this.dayLabel = dayLabel;
            this.totalWork = totalWork != null ? totalWork : java.math.BigDecimal.ZERO;
            this.sessionCount = sessionCount;
        }

        public String getDayLabel() { return dayLabel; }
        public java.math.BigDecimal getTotalWork() { return totalWork; }
        public int getSessionCount() { return sessionCount; }
    }

    public static class LeaveStat {
        private int totalRequests;
        private int totalDays;

        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int v) { this.totalRequests = v; }
        public int getTotalDays() { return totalDays; }
        public void setTotalDays(int v) { this.totalDays = v; }
    }

    public static class LeaveByTypeItem {
        private String leaveType;
        private int count;

        public LeaveByTypeItem(String leaveType, int count) {
            this.leaveType = leaveType;
            this.count = count;
        }

        public String getLeaveType() { return leaveType; }
        public int getCount() { return count; }
    }

    public static class LeaveStatusItem {
        private String statusName;
        private int count;

        public LeaveStatusItem(String statusName, int count) {
            this.statusName = statusName;
            this.count = count;
        }

        public String getStatusName() { return statusName; }
        public int getCount() { return count; }
    }

    public static class LeaveRow {
        private String fullName;
        private String leaveType;
        private String startDate;
        private String endDate;
        private int days;
        private String status;

        public LeaveRow(String fullName, String leaveType, String startDate,
                        String endDate, int days, String status) {
            this.fullName = fullName;
            this.leaveType = leaveType;
            this.startDate = startDate;
            this.endDate = endDate;
            this.days = days;
            this.status = status;
        }

        public String getFullName() { return fullName; }
        public String getLeaveType() { return leaveType; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public int getDays() { return days; }
        public String getStatus() { return status; }
    }

    public static class SalaryStat {
        private int employeeCount;
        private java.math.BigDecimal totalNet = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal avgNet = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal maxNet = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal minNet = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal totalInsurance = java.math.BigDecimal.ZERO;

        public int getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(int v) { this.employeeCount = v; }
        public java.math.BigDecimal getTotalNet() { return totalNet; }
        public void setTotalNet(java.math.BigDecimal v) { this.totalNet = v != null ? v : java.math.BigDecimal.ZERO; }
        public java.math.BigDecimal getAvgNet() { return avgNet; }
        public void setAvgNet(java.math.BigDecimal v) { this.avgNet = v != null ? v : java.math.BigDecimal.ZERO; }
        public java.math.BigDecimal getMaxNet() { return maxNet; }
        public void setMaxNet(java.math.BigDecimal v) { this.maxNet = v != null ? v : java.math.BigDecimal.ZERO; }
        public java.math.BigDecimal getMinNet() { return minNet; }
        public void setMinNet(java.math.BigDecimal v) { this.minNet = v != null ? v : java.math.BigDecimal.ZERO; }
        public java.math.BigDecimal getTotalInsurance() { return totalInsurance; }
        public void setTotalInsurance(java.math.BigDecimal v) { this.totalInsurance = v != null ? v : java.math.BigDecimal.ZERO; }
    }

    public static class SalaryRow {
        private String fullName;
        private String deptName;
        private String posName;
        private java.math.BigDecimal baseSalary;
        private java.math.BigDecimal netSalary;
        private java.math.BigDecimal actualWorkDays;
        private java.math.BigDecimal overtimeAmount;
        private java.math.BigDecimal totalInsurance;

        public SalaryRow(String fullName, String deptName, String posName,
                         java.math.BigDecimal baseSalary, java.math.BigDecimal netSalary,
                         java.math.BigDecimal actualWorkDays, java.math.BigDecimal overtimeAmount,
                         java.math.BigDecimal totalInsurance) {
            this.fullName = fullName;
            this.deptName = deptName;
            this.posName = posName;
            this.baseSalary = baseSalary != null ? baseSalary : java.math.BigDecimal.ZERO;
            this.netSalary = netSalary != null ? netSalary : java.math.BigDecimal.ZERO;
            this.actualWorkDays = actualWorkDays != null ? actualWorkDays : java.math.BigDecimal.ZERO;
            this.overtimeAmount = overtimeAmount != null ? overtimeAmount : java.math.BigDecimal.ZERO;
            this.totalInsurance = totalInsurance != null ? totalInsurance : java.math.BigDecimal.ZERO;
        }

        public String getFullName() { return fullName; }
        public String getDeptName() { return deptName; }
        public String getPosName() { return posName; }
        public java.math.BigDecimal getBaseSalary() { return baseSalary; }
        public java.math.BigDecimal getNetSalary() { return netSalary; }
        public java.math.BigDecimal getActualWorkDays() { return actualWorkDays; }
        public java.math.BigDecimal getOvertimeAmount() { return overtimeAmount; }
        public java.math.BigDecimal getTotalInsurance() { return totalInsurance; }
    }

    public static class RewardFineSummary {
        private BigDecimal totalAllowance = BigDecimal.ZERO;
        private int employeesWithAllowance;
        private BigDecimal totalFine = BigDecimal.ZERO;
        private int employeesWithFine;

        public BigDecimal getTotalAllowance() {
            return totalAllowance;
        }

        public void setTotalAllowance(BigDecimal totalAllowance) {
            this.totalAllowance = totalAllowance != null ? totalAllowance : BigDecimal.ZERO;
        }

        public int getEmployeesWithAllowance() {
            return employeesWithAllowance;
        }

        public void setEmployeesWithAllowance(int employeesWithAllowance) {
            this.employeesWithAllowance = employeesWithAllowance;
        }

        public BigDecimal getTotalFine() {
            return totalFine;
        }

        public void setTotalFine(BigDecimal totalFine) {
            this.totalFine = totalFine != null ? totalFine : BigDecimal.ZERO;
        }

        public int getEmployeesWithFine() {
            return employeesWithFine;
        }

        public void setEmployeesWithFine(int employeesWithFine) {
            this.employeesWithFine = employeesWithFine;
        }
    }
}

