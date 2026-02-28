package DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@ToString
@Getter
@Setter
public class DiscountDTO {
    private String code;
    private String name;
    private int type;
    private LocalDate startDate;
    private LocalDate endDate;

    // Constructors
    public DiscountDTO() {
    }

    public DiscountDTO(String code, String name, int type, LocalDate startDate, LocalDate endDate) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DiscountDTO(DiscountDTO discountDTO) {
        this.code = discountDTO.code;
        this.name = discountDTO.name;
        this.type = discountDTO.type;
        this.startDate = discountDTO.startDate;
        this.endDate = discountDTO.endDate;
    }

}
