package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePDFDTO {
    private int id;
    private LocalDateTime createdAt;
    private String employeeName; // JOIN từ Employee table
    private String customerName; // JOIN từ Customer table
    private String discountCode;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private ArrayList<DetailInvoicePDFDTO> details;
}
