package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailInvoicePDFDTO {
    private String productId;
    private String productName; // JOIN từ Product table
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}
