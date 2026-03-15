package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO for importing employees from Excel
 * Dùng cho việc import nhân viên từ file Excel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportEmployeeExcelDTO {
    private int rowNumber; // Dòng Excel (để báo lỗi)

    // Basic Info
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String gender; // Nam/Nữ

    // Position & Salary
    private Integer departmentId; // ID phòng ban (phải active)
    private Integer positionId; // ID vị trí (có baseSalary ở position, không phải employee)

    // Insurance & Benefits
    private String healthInsCode;
    private String socialInsCode;
    private String unemploymentInsCode;
    private Boolean mealSupport; // Y/N
    private Boolean transportSupport; // Y/N
    private Boolean accommodationSupport; // Y/N
    private Integer numDependents;
    private Integer roleId;

    // Account Info
    private String username;

    // Status & Validation
    private String errorMessage; // Để lưu lỗi validate
    private boolean isValid = true; // Cờ validate

    /**
     * Constructor cho việc set error
     */
    public ImportEmployeeExcelDTO(int rowNumber, String errorMessage) {
        this.rowNumber = rowNumber;
        this.errorMessage = errorMessage;
        this.isValid = false;
    }
}
