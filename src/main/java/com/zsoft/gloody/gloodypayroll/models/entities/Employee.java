package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.models.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Filter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "employee")
@Filter(name = "companyFilter", condition = "company_id = :companyId")
public class Employee {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "origin_id", nullable = false, unique = true)
    private Long originId;
    @Column(name = "registration_number")
    private String identification;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "middle_name")
    private String middleName;
    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(name = "family_situation")
    private MaritalStatus maritalStatus;
    @Column(name = "number_of_children")
    private Integer numberOfChildren;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    @ToString.Exclude
    private Address address;

    @Column(name = "social_security_number")
    private String socialSecurity;

    @Column(name = "payment_method")
    private PaymentMethod paymentMode;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "account_number")
    private String iban;

    @Column(name = "entered_date")
    private LocalDate hireDate;
    @Column(name = "contract_nature")
    private ContractNature contractNature;
    @Column(name = "job_classification")
    private String jobClassification;
    @Column(name = "job_category")
    private String jobCategory;
    @Column(name = "job_title")
    private String jobTitle;
    @Column(name = "department")
    private String department;
    @Column(name = "base_salary")
    private Double baseSalary;
    @Column(name = "salary_profile")
    @Enumerated(EnumType.STRING)
    private SalaryProfile remunerationType;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "company_id", updatable = false)
    @Builder.Default
    private Long company = 1L;

    public String getFullName() {
        return StringUtils.capitalize(Objects.requireNonNullElse(firstName, "")).concat(" ").concat(StringUtils.capitalize(Objects.requireNonNullElse(lastName, "")));
    }
}
