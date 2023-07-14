package com.employeemanagementsystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity(name = "Employee")
@Table(name = "employee")
public class Employee {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @NotBlank
    @Size(min = 1, max = 100)
    @Pattern(regexp = "[A-Za-z]+", message = "Only letters are allowed for name")
    @Column(name = "EMP_NAME")
    private String name;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "DOB")
    private LocalDate dob;

    @NotNull
    @Min(value = 101, message = "Manager ID should be between 101 and 105")
    @Max(value = 105, message = "Manager ID should be between 101 and 105")
    @Column(name = "MANAGER_ID")
    private Integer managerId;

    @NotNull
    @DecimalMin(value = "0.1", inclusive = false, message = "Salary should be greater than 0")
    @Digits(integer = 6, fraction = 2, message = "Salary should have at most 6 digits and 2 decimal places")
    @Column(name = "SALARY")
    private double salary;

    @NotBlank
    @Email(message = "Please provide a valid email address")
    @Column(name = "EMAIL_ID")
    private String emailId;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("employee")
    private List<PhoneNumber> phoneNumbers = new ArrayList<>();


    @OneToOne(fetch=FetchType.LAZY,mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("employee")
    private VoterID voterID;

    
    public Employee() {
        super();
    }
    
    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
        if (phoneNumbers != null) {
            this.phoneNumbers.clear();
            for (PhoneNumber phoneNumber : phoneNumbers) {
                if (!containsPhoneNumber(phoneNumber)) {
                    this.phoneNumbers.add(phoneNumber);
                    phoneNumber.setEmployee(this);
                }
            }
        }
    }

    private boolean containsPhoneNumber(PhoneNumber phoneNumber) {
        return this.phoneNumbers.stream()
                .anyMatch(pn -> pn.getPhoneNumber().equals(phoneNumber.getPhoneNumber())
                        && pn.getProvider().equals(phoneNumber.getProvider())
                        && pn.getType().equals(phoneNumber.getType()));
    }
    

    public VoterID getVoterID() {
        return voterID;
    }

    public void setVoterID(VoterID voterID) {
        this.voterID = voterID;
        if (voterID != null) {
            voterID.setEmployee(this);
        }

        // Update the employee reference for each phone number
        if (phoneNumbers != null) {
            for (PhoneNumber phoneNumber : phoneNumbers) {
                phoneNumber.setEmployee(this);
            }
        }
    }

    public void validate() throws ValidationException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Employee>> violations = validator.validate(this);

        if (!violations.isEmpty()) {
            StringBuilder errorBuilder = new StringBuilder();
            for (ConstraintViolation<Employee> violation : violations) {
                String fieldName = violation.getPropertyPath().toString();
                String errorMessage = violation.getMessage();
                errorBuilder.append(fieldName).append(": ").append(errorMessage).append("; ");
            }

            throw new ValidationException(errorBuilder.toString());
        }
    }
}
