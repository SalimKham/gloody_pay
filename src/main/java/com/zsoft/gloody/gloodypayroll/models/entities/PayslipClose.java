package com.zsoft.gloody.gloodypayroll.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "payslip_close")
public class PayslipClose {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payslip_close_sequence")
    @SequenceGenerator(
            name = "payslip_close_sequence",
            sequenceName = "payslip_close_sequence",
            allocationSize = 1
    )
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "code")
    private String sectionCode;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "month")
    private LocalDate month;

    @Column(name = "base_value")
    private Double baseValue;


    @Column(name = "rate_value")
    private Double rateValue;

    @Column(name = "value")
    private Double value;
}
