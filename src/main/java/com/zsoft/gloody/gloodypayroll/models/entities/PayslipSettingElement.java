package com.zsoft.gloody.gloodypayroll.models.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "payslip_setting_element")
public class PayslipSettingElement extends TenantSupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "payslip_setting_id")
    @ToString.Exclude
    private PayslipSetting payslipSetting;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "section_code")
    @ToString.Exclude
    private Section section;
    @Column(name = "value")
    private Double value;
    @Column(name = "base_value")
    private Double baseValue;
    @Column(name = "rate_value")
    private Double rateValue;
    @Column(name = "fix")
    private boolean fix;
}
