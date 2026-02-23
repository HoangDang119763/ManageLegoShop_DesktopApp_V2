package DTO;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountForInvoiceDTO {
    private String code;
    private String name;
    private int type;
    private ArrayList<DetailDiscountDTO> detailDiscountList;

    // Constructors
    public DiscountForInvoiceDTO() {
    }

    public DiscountForInvoiceDTO(String code, String name, int type) {
        this.code = code;
        this.name = name;
        this.type = type;

    }

    public DiscountForInvoiceDTO(String code, String name, int type, ArrayList<DetailDiscountDTO> detailDiscountList) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.detailDiscountList = detailDiscountList;
    }

    public DiscountForInvoiceDTO(DiscountForInvoiceDTO discountDTO) {
        this.code = discountDTO.code;
        this.name = discountDTO.name;
        this.type = discountDTO.type;
    }
}
