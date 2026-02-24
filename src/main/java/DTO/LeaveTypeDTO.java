package DTO;

import java.math.BigDecimal;

public class LeaveTypeDTO {
    private int id;
    private String name;
    private BigDecimal fineAmount;

    public LeaveTypeDTO() {
    }

    public LeaveTypeDTO(int id, String name, BigDecimal fineAmount) {
        this.id = id;
        this.name = name;
        this.fineAmount = fineAmount;
    }

    public LeaveTypeDTO(LeaveTypeDTO other) {
        this.id = other.id;
        this.name = other.name;
        this.fineAmount = other.fineAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    @Override
    public String toString() {
        return name;
    }
}
