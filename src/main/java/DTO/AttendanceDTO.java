package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

public class AttendanceDTO {
    private int id;
    private int employeeId;
    private String employeeName;
    private String departmentName;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private BigDecimal workHours;
    private BigDecimal otHours;
    private String status; // PRESENT, LATE, ABSENT...

    public AttendanceDTO() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }
    public BigDecimal getWorkHours() { return workHours; }
    public void setWorkHours(BigDecimal workHours) { this.workHours = workHours; }
    public BigDecimal getOtHours() { return otHours; }
    public void setOtHours(BigDecimal otHours) { this.otHours = otHours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Helper để lấy ngày từ checkIn phục vụ hiển thị cột colDate
    public LocalDate getDate() {
        return checkIn != null ? checkIn.toLocalDate() : null;
    }
}