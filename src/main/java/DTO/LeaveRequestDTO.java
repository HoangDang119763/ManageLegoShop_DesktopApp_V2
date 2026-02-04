package DTO;

import java.time.LocalDate;

public class LeaveRequestDTO {
    private int id;
    private String type;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private int statusId;
    private int employeeId;

    // Constructors
    public LeaveRequestDTO() {
    }

    public LeaveRequestDTO(int id, String type, String content, LocalDate startDate, LocalDate endDate, int statusId,
            int employeeId) {
        this.id = id;
        this.type = type;
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
            this.type = other.type;
            this.content = other.content;
            this.startDate = other.startDate;
            this.endDate = other.endDate;
            this.statusId = other.statusId;
            this.employeeId = other.employeeId;
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
