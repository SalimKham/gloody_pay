package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zsoft.gloody.gloodypayroll.models.enums.PayslipRate;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipSettingDto {
    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "employee")
    private EmployeeDto employee;
    @JsonProperty(value = "elements")
    private List<PayslipSettingElementDto> elements;
    @JsonProperty(value = "absences")
    private List<PayslipAbsenceDto> absences;

    @JsonProperty(value = "rate")
    @Builder.Default
    private PayslipRate rate = PayslipRate.DAILY;

    @JsonProperty(value = "show_intermediate")
    @Builder.Default
    private boolean showIntermediate = false;

    @JsonProperty(value = "payment_date")
    @Builder.Default
    private LocalDate paymentDate = LocalDate.now();
}
