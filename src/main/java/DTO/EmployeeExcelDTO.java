package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeExcelDTO {
    private int id;
    private String fullName; // JOIN firstName + lastName
    private String gender;
    private String positionName; // JOIN Position
    private BigDecimal wage;
    private String username; // JOIN Account.username
    private String statusDescription; // JOIN Status.description
    private String departmentName; // JOIN Department.name
    private String healthInsCode;
    private String socialInsCode;
    private String unemploymentInsCode;
    private boolean isMealSupport;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;
    private int numDependents;
}
