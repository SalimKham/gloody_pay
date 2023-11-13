package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipSettingElementDto {
    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "payslip_setting_id")
    private Long payslipSettingId;
    @JsonProperty(value = "section_code")
    private String section;
    @JsonProperty(value = "value")
    private Double value;
    @JsonProperty(value = "base_value")
    private Double baseValue;
    @JsonProperty(value = "rate_value")
    private Double rateValue;
    @JsonProperty(value = "fix")
    private boolean fix;
}
