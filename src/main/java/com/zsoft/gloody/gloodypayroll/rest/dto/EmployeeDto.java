package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.zsoft.gloody.gloodypayroll.models.enums.EmployeeTitle;
import com.zsoft.gloody.gloodypayroll.models.enums.MaritalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private boolean disabled;
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;
    private Integer numberOfChildren;
    @Column(name = "gender")
    private EmployeeTitle employeeTitle;
    private AddressDto address;
    private String socialSecurity;
    private String paymentMode;
    private String bankName;
    private String accountNumber;
    private LocalDate hireDate;
    private LocalDate birthDate;
    private LocalDate seniority_date;
    private LocalDate exitDate;
    private String contractNature;
    private String jobClassification;
    private String jobCategory;
    private String jobTitle;
    private String department;
    private String affiliate;
    private String office;
    private Integer seniorityOrderNumber;
    @Builder.Default
    private Double baseSalary = 0d;
    private String remunerationType;
    @NotNull
    private Long company;
    @Builder.Default
    List<CustomFieldDto> customFields  = new ArrayList();


    public Double getBaseSalary() {
        return Objects.requireNonNullElse(baseSalary, 0d);
    }

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
        return String.format("%s%s", Objects.nonNull(firstName) ? StringUtils.capitalize(firstName) : "", Objects.nonNull(lastName) ? StringUtils.capitalize(lastName) : "");
    }
}
