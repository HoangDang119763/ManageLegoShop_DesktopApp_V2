package DTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    // Constructor cơ bản (không join)
    public LeaveRequestDTO(
            int id,
            int leaveTypeId,
            String content,
            LocalDate startDate,
            LocalDate endDate,
            int statusId,
            int employeeId) {

        this(id, leaveTypeId, "", content, startDate, endDate, statusId, "", employeeId, "");
    }

    // Constructor có leaveTypeName
    public LeaveRequestDTO(
            int id,
            int leaveTypeId,
            String leaveTypeName,
            String content,
            LocalDate startDate,
            LocalDate endDate,
            int statusId,
            int employeeId) {

        this(id, leaveTypeId, leaveTypeName, content, startDate, endDate, statusId, "", employeeId, "");
    }

    // ✅ Constructor đầy đủ (JOIN leave_type + status + employee)
    public LeaveRequestDTO(
            int id,
            int leaveTypeId,
            String leaveTypeName,
            String content,
            LocalDate startDate,
            LocalDate endDate,
            int statusId,
            String statusName,
            int employeeId) {

        this(id, leaveTypeId, leaveTypeName, content, startDate, endDate, statusId, statusName, employeeId, "");
    }

    // ✅ Constructor FULL (JOIN tất cả)
    public LeaveRequestDTO(
            int id,
            int leaveTypeId,
            String leaveTypeName,
            String content,
            LocalDate startDate,
            LocalDate endDate,
            int statusId,
            String statusName,
            int employeeId,
            String employeeName) {

        this.id = id;
        this.leaveTypeId = leaveTypeId;
        this.leaveTypeName = leaveTypeName;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.statusId = statusId;
        this.statusName = statusName;
        this.employeeId = employeeId;
        this.employeeName = employeeName;

        calculateDayCount();
    }

    // Copy constructor
    public LeaveRequestDTO(LeaveRequestDTO other) {

        this.id = other.id;
        this.leaveTypeId = other.leaveTypeId;
        this.leaveTypeName = other.leaveTypeName;
        this.content = other.content;
        this.startDate = other.startDate;
        this.endDate = other.endDate;
        this.statusId = other.statusId;
        this.statusName = other.statusName;
        this.employeeId = other.employeeId;
        this.employeeName = other.employeeName;
        this.dayCount = other.dayCount;
    }

    // ========================
    // Logic
    // ========================

    private void calculateDayCount() {

        if (startDate != null && endDate != null) {

            dayCount = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        } else {
            dayCount = 0;
        }
    }

    // ========================
    // Getters / Setters
    // ========================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(int leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
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
        calculateDayCount();
    }


    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        calculateDayCount();
    }


    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }


    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }


    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
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


    // Optional boolean helper
    public boolean isApproved() {
        return statusId == 1;
    }

}