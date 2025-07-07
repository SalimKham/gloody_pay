package com.zsoft.gloody.gloodypayroll.rest.dto;
import com.zsoft.gloody.gloodypayroll.models.enums.PayslipRate;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipSettingDto implements Cloneable{
    private boolean template;
    private String name;
    private Long id;
    private EmployeeDto employee;
    private ContractDto contract;
    private List<PayslipSettingElementDto> elements;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PayslipRate rate = PayslipRate.DAILY;
    @Builder.Default
    private boolean showIntermediate = false;
    @Builder.Default
    private boolean showZero= true;
    @Builder.Default
    private LocalDate paymentDate = LocalDate.now();
    private boolean closed;
    private boolean approved;
    private boolean byDefault;
    private PayslipDetailDto payslipDetail;
    @Builder.Default
    private Boolean draft = false;

    @Builder.Default
    private Boolean referenced = false;

    @Override
    public Object clone() throws CloneNotSupportedException {
        PayslipSettingDto cloned = (PayslipSettingDto) super.clone();
        if (this.elements != null) {
            List<PayslipSettingElementDto> els = new ArrayList<>(this.elements.size());
            for (PayslipSettingElementDto el: this.elements) {
                els.add((PayslipSettingElementDto) el.clone());
            }
            cloned.setElements(els);
        }
        return cloned;
    }

}
