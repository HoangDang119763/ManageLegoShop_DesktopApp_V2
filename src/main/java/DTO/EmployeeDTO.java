package DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class EmployeeDTO extends BaseInformationDTO {
    private String firstName;
    private String lastName;
    private String email;
    private int roleId;
    private Integer departmentId; // Phòng ban
    private String gender;
    private Integer accountId; // Dùng Integer để có thể nhận giá trị null
    private String healthInsCode; // Mã BHYT
    private String socialInsCode;
    private String unemploymentInsCode;

    // Các cờ hiệu bảo hiểm & phụ cấp (tinyint 1 -> boolean)
    private boolean isSocialInsurance;
    private boolean isUnemploymentInsurance;
    private boolean isPersonalIncomeTax;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;
    private String avatarUrl; // Đường dẫn ảnh đại diện

    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeDTO() {
        super();
    }

    // Constructor đầy đủ cho việc lấy dữ liệu từ DB
    public EmployeeDTO(int id, String firstName, String lastName, String phone, String email,
            LocalDate dateOfBirth, int roleId, Integer departmentId, int statusId,
            String gender, Integer accountId, String healthInsCode,
            boolean isSocialInsurance, boolean isUnemploymentInsurance,
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
        this.healthInsCode = healthInsCode;
        this.socialInsCode = isSocialInsurance ? "1" : "0";
        this.unemploymentInsCode = isUnemploymentInsurance ? "1" : "0";
        this.isPersonalIncomeTax = isPersonalIncomeTax;
        this.isTransportationSupport = isTransportationSupport;
        this.isAccommodationSupport = isAccommodationSupport;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor đầy đủ với avatarUrl (New)
    public EmployeeDTO(int id, String firstName, String lastName, String phone, String email,
            LocalDate dateOfBirth, int roleId, Integer departmentId, int statusId,
            String gender, Integer accountId, String healthInsCode,
            boolean isSocialInsurance, boolean isUnemploymentInsurance,
            boolean isPersonalIncomeTax, boolean isTransportationSupport,
            boolean isAccommodationSupport, LocalDateTime createdAt, LocalDateTime updatedAt,
            String avatarUrl) {
        super(id, dateOfBirth, phone, statusId, updatedAt);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roleId = roleId;
        this.departmentId = departmentId;
        this.gender = gender;
        this.accountId = accountId;
        this.healthInsCode = healthInsCode;
        this.isSocialInsurance = isSocialInsurance;
        this.isUnemploymentInsurance = isUnemploymentInsurance;
        this.isPersonalIncomeTax = isPersonalIncomeTax;
        this.isTransportationSupport = isTransportationSupport;
        this.isAccommodationSupport = isAccommodationSupport;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.avatarUrl = avatarUrl;
    }

    public EmployeeDTO(int id, String firstName, String lastName, String phone, String email,
            LocalDate dateOfBirth, int roleId, Integer departmentId, int statusId,
            String gender, Integer accountId, String healthInsCode,
            boolean isSocialInsurance, boolean isUnemploymentInsurance,
            boolean isPersonalIncomeTax, boolean isTransportationSupport,
            boolean isAccommodationSupport) {
        super(id, dateOfBirth, phone, statusId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roleId = roleId;
        this.departmentId = departmentId;
        this.gender = gender;
        this.accountId = accountId;
        this.healthInsCode = healthInsCode;
        this.isSocialInsurance = isSocialInsurance;
        this.isUnemploymentInsurance = isUnemploymentInsurance;
        this.isPersonalIncomeTax = isPersonalIncomeTax;
        this.isTransportationSupport = isTransportationSupport;
        this.isAccommodationSupport = isAccommodationSupport;
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
        this.statusId = other.statusId;
        this.healthInsCode = other.healthInsCode;
        this.isSocialInsurance = other.isSocialInsurance;
        this.isUnemploymentInsurance = other.isUnemploymentInsurance;
        this.isPersonalIncomeTax = other.isPersonalIncomeTax;
        this.isTransportationSupport = other.isTransportationSupport;
        this.isAccommodationSupport = other.isAccommodationSupport;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        this.avatarUrl = other.avatarUrl;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
    }

    public String getFullName() {
        return (this.firstName != null ? this.firstName : "") + " " + (this.lastName != null ? this.lastName : "");
    }

    public boolean isHealthInsurance() {
        return healthInsCode != null && !healthInsCode.isEmpty();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getHealthInsCode() {
        return healthInsCode;
    }

    public void setHealthInsCode(String healthInsCode) {
        this.healthInsCode = healthInsCode;
    }

    public String getSocialInsCode() {
        return socialInsCode;
    }

    public void setSocialInsCode(String socialInsCode) {
        this.socialInsCode = socialInsCode;
    }

    public String getUnemploymentInsCode() {
        return unemploymentInsCode;
    }

    public void setUnemploymentInsCode(String unemploymentInsCode) {
        this.unemploymentInsCode = unemploymentInsCode;
    }

    public boolean isSocialInsurance() {
        return isSocialInsurance;
    }

    public void setSocialInsurance(boolean socialInsurance) {
        isSocialInsurance = socialInsurance;
    }

    public boolean isUnemploymentInsurance() {
        return isUnemploymentInsurance;
    }

    public void setUnemploymentInsurance(boolean unemploymentInsurance) {
        isUnemploymentInsurance = unemploymentInsurance;
    }

    public boolean isPersonalIncomeTax() {
        return isPersonalIncomeTax;
    }

    public void setPersonalIncomeTax(boolean personalIncomeTax) {
        isPersonalIncomeTax = personalIncomeTax;
    }

    public boolean isTransportationSupport() {
        return isTransportationSupport;
    }

    public void setTransportationSupport(boolean transportationSupport) {
        isTransportationSupport = transportationSupport;
    }

    public boolean isAccommodationSupport() {
        return isAccommodationSupport;
    }

    public void setAccommodationSupport(boolean accommodationSupport) {
        isAccommodationSupport = accommodationSupport;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return this.getId() + " - " + getFullName();
    }

}