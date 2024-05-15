package com.zsoft.gloody.gloodypayroll.rest.dto;
import com.zsoft.gloody.gloodypayroll.models.enums.MaritalStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmployeeDto {
    private Long id;
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
}
