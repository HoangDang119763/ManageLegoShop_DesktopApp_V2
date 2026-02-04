package DTO;

import java.time.LocalDate;

public class HolidayDTO {
    private int id;
    private String name;
    private LocalDate date;

    public HolidayDTO() {
    }

    public HolidayDTO(int id, String name, LocalDate date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }

    public HolidayDTO(HolidayDTO other) {
        this.id = other.id;
        this.name = other.name;
        this.date = other.date;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "HolidayDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}
