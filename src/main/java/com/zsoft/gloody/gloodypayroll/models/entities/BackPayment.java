package com.zsoft.gloody.gloodypayroll.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "back_payment")
public class BackPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "code")
    private String code;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payslip_setting_element_id", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    private PayslipSettingElement element;
    @Column(name = "value")
    private Double value;
}
