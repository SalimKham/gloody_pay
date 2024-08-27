package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.models.enums.PayslipRate;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipDetailDto;
import com.zsoft.gloody.gloodypayroll.support.PayslipDetailConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

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
@NamedEntityGraph(name = "PayslipSetting.elements",
        attributeNodes = @NamedAttributeNode(value = "elements", subgraph = "PayslipSetting.elements.section"), subgraphs = {
        @NamedSubgraph(name = "PayslipSetting.elements.section", attributeNodes = {
                @NamedAttributeNode(value = "section")
        })
})
public class PayslipSetting extends CompanySupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "employee_id")
    private Long employee;

    @Column(name = "show_intermediate")
    private boolean showIntermediate;

    @Column(name = "show_zero")
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

    @Column(name="payslip_rate")
    private PayslipRate payslipRate;

    @Enumerated(EnumType.STRING)
    @Formula("(SELECT CASE WHEN payslip_rate is NOT NULL THEN payslip_rate WHEN EXISTS(SELECT v.id from variable_setting v where v.key = 'IS_HOURLY_RATE' AND v.formula = '1') THEN 'HOURLY' ELSE 'DAILY' END)")
    private PayslipRate rate;

    @Convert(converter = PayslipDetailConverter.class)
    @Column(name = "payslip_detail")
    private PayslipDetailDto payslipDetail;


    @OneToMany(targetEntity = PayslipSettingElement.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payslip_setting_id", nullable = false)
    @ToString.Exclude
    @OrderBy("elementOrder asc")
    private List<PayslipSettingElement> elements;

}
