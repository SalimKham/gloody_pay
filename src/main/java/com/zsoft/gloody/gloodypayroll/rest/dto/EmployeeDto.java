package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmployeeDto {
    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "registration_number")
    private String registrationNumber;
    @JsonProperty(value = "last_name")
    private String lastName;
    @JsonProperty(value = "first_name")
    private String firstName;
    @JsonProperty(value = "middle_name")
    private String middleName;
    @JsonProperty(value = "gender")
    private String gender;
    @JsonProperty(value = "family_situation")
    private String familySituation;
    @JsonProperty(value = "number_of_children")
    private Integer numberOfChildren;
    @JsonProperty(value = "address")
    private AddressDto address;
    @JsonProperty(value = "identity_number")
    private String identityNumber;
    @JsonProperty(value = "social_security_number")
    private String socialSecurityNumber;

    @JsonProperty(value = "payment_method")
    private String paymentMethod;
    @JsonProperty(value = "bank_name")
    private String bankName;
    @JsonProperty(value = "account_number")
    private String accountNumber;

    @JsonProperty(value = "entered_date")
    private LocalDate enteredDate;
    @JsonProperty(value = "contract_nature")
    private String contractNature;
    @JsonProperty(value = "job_classification")
    private String jobClassification;
    @JsonProperty(value = "job_category")
    private String jobCategory;
    @JsonProperty(value = "job_title")
    private String jobTitle;
    @JsonProperty(value = "department")
    private String department;
    @JsonProperty(value = "base_salary")
    private Double baseSalary;
    @JsonProperty(value = "salary_profile")
    private String salaryProfile;

    @JsonProperty(value = "company_id")
    private Long companyId;
}
