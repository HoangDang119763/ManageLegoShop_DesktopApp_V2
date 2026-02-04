package FACTORY;

import DTO.EmployeeDTO;
import INTERFACE.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeBuilder implements Builder<EmployeeDTO> {
    private int id;
    private int statusId;
    private LocalDate dateOfBirth;
    private String firstName;
    private String lastName;
    private String phone;
    private String email = "";
    private int roleId;
    private String gender = "";
    private int positionId = 0;
    private Integer accountId = null;
    private boolean isHealthInsurance = false;
    private boolean isSocialInsurance = false;
    private boolean isUnemploymentInsurance = false;
    private boolean isPersonalIncomeTax = false;
    private boolean isTransportationSupport = false;
    private boolean isAccommodationSupport = false;

    public EmployeeBuilder id(int id) {
        this.id = id;
        return this;
    }

    public EmployeeBuilder statusId(int statusId) {
        this.statusId = statusId;
        return this;
    }

    public EmployeeBuilder dateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public EmployeeBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public EmployeeBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public EmployeeBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public EmployeeBuilder roleId(int roleId) {
        this.roleId = roleId;
        return this;
    }

    public EmployeeBuilder email(String email) {
        this.email = email;
        return this;
    }

    public EmployeeBuilder gender(String gender) {
        this.gender = gender;
        return this;
    }

    public EmployeeBuilder positionId(int positionId) {
        this.positionId = positionId;
        return this;
    }

    public EmployeeBuilder accountId(Integer accountId) {
        this.accountId = accountId;
        return this;
    }

    public EmployeeBuilder isHealthInsurance(boolean isHealthInsurance) {
        this.isHealthInsurance = isHealthInsurance;
        return this;
    }

    public EmployeeBuilder isSocialInsurance(boolean isSocialInsurance) {
        this.isSocialInsurance = isSocialInsurance;
        return this;
    }

    public EmployeeBuilder isUnemploymentInsurance(boolean isUnemploymentInsurance) {
        this.isUnemploymentInsurance = isUnemploymentInsurance;
        return this;
    }

    public EmployeeBuilder isPersonalIncomeTax(boolean isPersonalIncomeTax) {
        this.isPersonalIncomeTax = isPersonalIncomeTax;
        return this;
    }

    public EmployeeBuilder isTransportationSupport(boolean isTransportationSupport) {
        this.isTransportationSupport = isTransportationSupport;
        return this;
    }

    public EmployeeBuilder isAccommodationSupport(boolean isAccommodationSupport) {
        this.isAccommodationSupport = isAccommodationSupport;
        return this;
    }

    @Override
    public EmployeeDTO build() {
        return new EmployeeDTO(id, firstName, lastName, phone, email,
                dateOfBirth, roleId, statusId,
                gender, positionId, accountId, isHealthInsurance,
                isSocialInsurance, isUnemploymentInsurance,
                isPersonalIncomeTax, isTransportationSupport,
                isAccommodationSupport);
    }
}
