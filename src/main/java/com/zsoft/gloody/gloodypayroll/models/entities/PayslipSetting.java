package com.zsoft.gloody.gloodypayroll.models.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "payslip_setting")
public class PayslipSetting extends TenantSupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    @ToString.Exclude
    private Employee employee;

    @Column(name = "show_intermediate")
    private boolean showIntermediate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "payslipSetting", orphanRemoval = true)
    @ToString.Exclude
    private List<PayslipSettingElement> elements;
}
