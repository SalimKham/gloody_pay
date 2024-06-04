package com.zsoft.gloody.gloodypayroll.rest.dto;
import com.zsoft.gloody.gloodypayroll.models.enums.PayslipRate;
import lombok.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipSettingDto {
    private boolean template;
    private String name;
    private Long id;
    private EmployeeDto employee;
    private List<PayslipSettingElementDto> elements;
    private PayslipAbsenceDto absences;
    @Builder.Default
    private PayslipRate rate = PayslipRate.DAILY;
    @Builder.Default
    private boolean showIntermediate = false;
    @Builder.Default
    private LocalDate paymentDate = LocalDate.now();
    private List<VariableSettingDto> variables = Collections.emptyList();
}
