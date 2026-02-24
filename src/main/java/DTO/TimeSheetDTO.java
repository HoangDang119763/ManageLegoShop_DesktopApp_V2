package DTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Duration;

public class TimeSheetDTO {
    private int id;
    private int employeeId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private BigDecimal workHours;
    private BigDecimal otHours;

    public TimeSheetDTO() {
    }

    public TimeSheetDTO(int id, int employeeId, LocalDateTime checkIn, LocalDateTime checkOut, BigDecimal workHours) {
        this.id = id;
        this.employeeId = employeeId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.workHours = workHours;
        this.otHours = BigDecimal.ZERO;
    }

    public TimeSheetDTO(int id, int employeeId, LocalDateTime checkIn, LocalDateTime checkOut, BigDecimal workHours, BigDecimal otHours) {
        this.id = id;
        this.employeeId = employeeId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.workHours = workHours;
        this.otHours = otHours;
    }

    public TimeSheetDTO(TimeSheetDTO other) {
        this.id = other.id;
        this.employeeId = other.employeeId;
        this.checkIn = other.checkIn;
        this.checkOut = other.checkOut;
        this.workHours = other.workHours;
        this.otHours = other.otHours;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }

    public BigDecimal getOtHours() {
        return otHours != null ? otHours : BigDecimal.ZERO;
    }

    public void setOtHours(BigDecimal otHours) {
        this.otHours = otHours;
    }

    public BigDecimal calculateWorkHours() {
        if (checkIn == null || checkOut == null) {
            return BigDecimal.ZERO;
        }

        // Tính tổng số phút chênh lệch
        long totalMinutes = Duration.between(checkIn, checkOut).toMinutes();

        // Xử lý trường hợp checkOut trước checkIn (nếu có lỗi dữ liệu)
        if (totalMinutes < 0)
            return BigDecimal.ZERO;

        // Lấy tổng phút chia cho 60, làm tròn 2 chữ số thập phân
        return BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "TimeSheetDTO{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", checkIn=" + checkIn +
                ", checkOut=" + checkOut +
                ", workHours=" + workHours +
                ", otHours=" + otHours +
                '}';
    }
}
