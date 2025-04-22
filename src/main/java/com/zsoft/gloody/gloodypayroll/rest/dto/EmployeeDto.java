package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.zsoft.gloody.gloodypayroll.models.enums.MaritalStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmployeeDto {
    private Long id;
    private Long originId;
    private String identification;
    private String lastName;
    private String firstName;
    private String middleName;
    private String title;
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;
    private Integer numberOfChildren;
    private AddressDto address;
    private String socialSecurity;
    private String paymentMode;
    private String bankName;
    private String iban;
    private LocalDate hireDate;
    private String contractNature;
    private String jobClassification;
    private String jobCategory;
    private String jobTitle;
    private String department;
    private Double baseSalary;
    private String remunerationType;
    @NotNull
    private CompanyDto company;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmployeeDto employeeDto = (EmployeeDto) o;
        if (employeeDto.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), employeeDto.getId());
    }

    public String getFullName() {
        return String.format("%s%s", Objects.nonNull(firstName) ? StringUtils.capitalize(firstName) : "",  Objects.nonNull(lastName) ? StringUtils.capitalize(lastName) :"");
    }
}
