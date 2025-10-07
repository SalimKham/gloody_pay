package com.zsoft.gloody.gloodypayroll.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

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

    @OneToMany(targetEntity = BackPayment.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payslip_setting_element_id", nullable = false)
    @ToString.Exclude
    private List<BackPayment> backPayments;
}
