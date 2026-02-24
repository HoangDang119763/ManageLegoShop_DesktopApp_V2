package DTO;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempDetailImportDTO {
    private String productId;
    private String name;
    private int quantity;
    private BigDecimal profitPercent;
    private BigDecimal importPrice;
    private BigDecimal totalPrice;

    public TempDetailImportDTO() {
    }

    public TempDetailImportDTO(String productId, String name, int quantity,
            BigDecimal profitPercent, BigDecimal importPrice, BigDecimal totalPrice) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.profitPercent = profitPercent;
        this.importPrice = importPrice;
        this.totalPrice = totalPrice;
    }

    public TempDetailImportDTO(TempDetailImportDTO other) {
        this.productId = other.productId;
        this.name = other.name;
        this.quantity = other.quantity;
        this.profitPercent = other.profitPercent;
        this.importPrice = other.importPrice;
        this.totalPrice = other.totalPrice;
    }

}
