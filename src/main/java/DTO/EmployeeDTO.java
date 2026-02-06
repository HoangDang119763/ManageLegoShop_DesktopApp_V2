package DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
public class EmployeeDTO extends BaseInformationDTO {
    private String firstName;
    private String lastName;
    private String email;
    private int roleId;
    private String gender;
    private Integer accountId; // Dùng Integer để có thể nhận giá trị null
    private String healthInsCode; // Mã BHYT

    // Các cờ hiệu bảo hiểm & phụ cấp (tinyint 1 -> boolean)
    private boolean isSocialInsurance;
    private boolean isUnemploymentInsurance;
    private boolean isPersonalIncomeTax;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;

    // Thời gian cập nhật
    private LocalDateTime updatedAt;

    public EmployeeDTO() {
        super();
    }

    // Constructor đầy đủ cho việc lấy dữ liệu từ DB
    public EmployeeDTO(int id, String firstName, String lastName, String phone, String email,
            LocalDate dateOfBirth, int roleId, int statusId,
            String gender, Integer accountId, String healthInsCode,
            boolean isSocialInsurance, boolean isUnemploymentInsurance,
            boolean isPersonalIncomeTax, boolean isTransportationSupport,
            boolean isAccommodationSupport, LocalDateTime updatedAt) {
        super(id, dateOfBirth, phone, statusId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roleId = roleId;
        this.gender = gender;
        this.accountId = accountId;
        this.healthInsCode = healthInsCode;
        this.isSocialInsurance = isSocialInsurance;
        this.isUnemploymentInsurance = isUnemploymentInsurance;
        this.isPersonalIncomeTax = isPersonalIncomeTax;
        this.isTransportationSupport = isTransportationSupport;
        this.isAccommodationSupport = isAccommodationSupport;
        this.updatedAt = updatedAt;
    }

    // Copy Constructor (Clone)
    public EmployeeDTO(EmployeeDTO other) {
        super(other);
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.email = other.email;
        this.roleId = other.roleId;
        this.gender = other.gender;
        this.accountId = other.accountId;
        this.statusId = other.statusId;
        this.healthInsCode = other.healthInsCode;
        this.isSocialInsurance = other.isSocialInsurance;
        this.isUnemploymentInsurance = other.isUnemploymentInsurance;
        this.isPersonalIncomeTax = other.isPersonalIncomeTax;
        this.isTransportationSupport = other.isTransportationSupport;
        this.isAccommodationSupport = other.isAccommodationSupport;
        this.updatedAt = other.updatedAt;
    }

    public String getFullName() {
        return (this.firstName != null ? this.firstName : "") + " " + (this.lastName != null ? this.lastName : "");
    }

    public boolean isHealthInsurance() {
        return healthInsCode != null && !healthInsCode.isEmpty();
    }

}