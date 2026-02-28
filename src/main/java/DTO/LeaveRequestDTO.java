package DTO;

import java.time.LocalDate;

public class LeaveRequestDTO {
    private int id;
    private int leaveTypeId;
    private String leaveTypeName;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private int statusId;
    private int employeeId;
    private String employeeName;
    private int dayCount;
    private String statusName;

    // Constructors
    public LeaveRequestDTO() {
    }

    // Constructor without leaveTypeName (for BaseDAL getAll when not JOINed)
    public LeaveRequestDTO(int id, int leaveTypeId, String content, LocalDate startDate, LocalDate endDate, int statusId,
            int employeeId) {
        this(id, leaveTypeId, "", content, startDate, endDate, statusId, employeeId);
    }

    // Constructor with leaveTypeName
    public LeaveRequestDTO(int id, int leaveTypeId, String leaveTypeName, String content, LocalDate startDate, LocalDate endDate, int statusId,
            int employeeId) {
        this.id = id;
        this.leaveTypeId = leaveTypeId;
        this.leaveTypeName = leaveTypeName;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.statusId = statusId;
        this.employeeId = employeeId;
    }

    // Copy Constructor
    public LeaveRequestDTO(LeaveRequestDTO other) {
        if (other != null) {
            this.id = other.id;
            this.leaveTypeId = other.leaveTypeId;
            this.leaveTypeName = other.leaveTypeName;
            this.content = other.content;
            this.startDate = other.startDate;
            this.endDate = other.endDate;
            this.statusId = other.statusId;
            this.employeeId = other.employeeId;
        }
    }

    // Getters and Setters
    public int getLeaveTypeId() {
        return leaveTypeId;
    }
    public void setLeaveTypeId(int leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLeaveTypeName() {
        return leaveTypeName;
    }

    public void setLeaveTypeName(String leaveTypeName) {
        this.leaveTypeName = leaveTypeName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public boolean isStatus() {
        return statusId == 1;
    }

    public void setStatus(boolean status) {
        this.statusId = status ? 1 : 0;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
