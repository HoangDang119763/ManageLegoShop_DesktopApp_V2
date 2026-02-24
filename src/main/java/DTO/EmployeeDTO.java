package DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class EmployeeDTO extends BaseInformationDTO {
    private String firstName;
    private String lastName;
    private String email;
    private int roleId;
    private Integer departmentId;
    private String gender;
    private Integer accountId;
    private String avatarUrl;

    // Định danh bảo hiểm (Lưu String theo DB mới)
    private String healthInsCode;
    private String socialInsCode;
    private String unemploymentInsCode;

    // Các cờ hiệu phúc lợi (Dạng boolean khớp với TINYINT 1)
    private boolean isPersonalIncomeTax;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeDTO() {
        super();
    }

    // Constructor dùng cho View/List cơ bản
    public EmployeeDTO(int id, String firstName, String lastName, String phone, String email,
            LocalDate dateOfBirth, int statusId) {
        super(id, dateOfBirth, phone, statusId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Constructor đầy đủ nhất để Map dữ liệu từ Database
    public EmployeeDTO(int id, String firstName, String lastName, String phone, String email,
            LocalDate dateOfBirth, int roleId, Integer departmentId, int statusId,
            String gender, Integer accountId, String avatarUrl,
            String healthInsCode, String socialInsCode, String unemploymentInsCode,
            boolean isPersonalIncomeTax, boolean isTransportationSupport,
            boolean isAccommodationSupport, LocalDateTime createdAt, LocalDateTime updatedAt) {

        super(id, dateOfBirth, phone, statusId, updatedAt);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roleId = roleId;
        this.departmentId = departmentId;
        this.gender = gender;
        this.accountId = accountId;
        this.avatarUrl = avatarUrl;
        this.healthInsCode = healthInsCode;
        this.socialInsCode = socialInsCode;
        this.unemploymentInsCode = unemploymentInsCode;
        this.isPersonalIncomeTax = isPersonalIncomeTax;
        this.isTransportationSupport = isTransportationSupport;
        this.isAccommodationSupport = isAccommodationSupport;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Copy Constructor (Clone)
    public EmployeeDTO(EmployeeDTO other) {
        super(other);
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.email = other.email;
        this.roleId = other.roleId;
        this.departmentId = other.departmentId;
        this.gender = other.gender;
        this.accountId = other.accountId;
        this.avatarUrl = other.avatarUrl;
        this.healthInsCode = other.healthInsCode;
        this.socialInsCode = other.socialInsCode;
        this.unemploymentInsCode = other.unemploymentInsCode;
        this.isPersonalIncomeTax = other.isPersonalIncomeTax;
        this.isTransportationSupport = other.isTransportationSupport;
        this.isAccommodationSupport = other.isAccommodationSupport;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
    }

    // ==================== HELPER METHODS ====================

    public String getFullName() {
        return (this.firstName != null ? this.firstName : "") + " " + (this.lastName != null ? this.lastName : "");
    }

    /**
     * Kiểm tra có tham gia bảo hiểm hay không (dùng cho UI CheckBox)
     * Logic: Nếu code khác null và khác "0" thì coi như có tham gia.
     */
    public boolean isHealthInsurance() {
        return healthInsCode != null && !healthInsCode.trim().isEmpty() && !"0".equals(healthInsCode);
    }

    public boolean isSocialInsurance() {
        return socialInsCode != null && !socialInsCode.trim().isEmpty() && !"0".equals(socialInsCode);
    }

    public boolean isUnemploymentInsurance() {
        return unemploymentInsCode != null && !unemploymentInsCode.trim().isEmpty() && !"0".equals(unemploymentInsCode);
    }

    @Override
    public String toString() {
        return this.getId() + " - " + getFullName();
    }
}