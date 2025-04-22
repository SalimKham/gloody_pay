package com.zsoft.gloody.gloodypayroll.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_monthly_data")
public class HRMonthlyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resource_id")
    private Long resourceId;
    @Column(name = "contract_id")
    private Long contractId;
    @Column(name = "pay_month")
    private LocalDate month;
    @Column(name = "hr_variable")
    private String variable;
    @Column(name = "hr_value")
    private double value;
}
