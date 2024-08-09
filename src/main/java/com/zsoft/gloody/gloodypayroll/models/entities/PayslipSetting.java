package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipDetailDto;
import com.zsoft.gloody.gloodypayroll.support.PayslipDetailConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
public class PayslipSetting extends CompanySupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "employee_id")
    private Long employee;

    @Column(name = "show_intermediate")
    private boolean showIntermediate;

    @Column(name="show_zero")
    private boolean showZero;

    @Column(name = "template")
    private boolean template;

    @Column(name = "is_default")
    private boolean byDefault;

    @Column(name = "name")
    private String name;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "closed")
    private boolean closed;

    @Column(name = "approved")
    private boolean approved;

    @Convert(converter = PayslipDetailConverter.class)
    @Column(name="payslip_detail")
    private PayslipDetailDto payslipDetail;

    @OneToMany(targetEntity = PayslipSettingElement.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payslip_setting_id", nullable = false)
    @ToString.Exclude
    @OrderBy("elementOrder asc")
    private List<PayslipSettingElement> elements;
}
