package com.zsoft.gloody.gloodypayroll.models.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "payslip_setting_element")
public class PayslipSettingElement extends CompanySupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payslip_setting_id", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    private PayslipSetting payslipSetting;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section")
    private Section section;

    @Formula("(SELECT s.code from section s where s.id = section )")
    private String sectionCode;
    @Column(name = "value")
    private Double value;
    @Column(name = "base_value")
    private Double baseValue;
    @Column(name = "rate_value")
    private Double rateValue;
    @Column(name = "fix")
    private boolean fix;
    @Column(name = "element_order")
    private int elementOrder;
}
