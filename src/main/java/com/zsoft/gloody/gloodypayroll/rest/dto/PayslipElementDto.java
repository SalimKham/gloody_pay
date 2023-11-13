package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipElementDto {
    @JsonProperty(value = "code")
    private String code;
    @JsonProperty(value = "label")
    private String label;
    @JsonProperty(value = "description")
    private String description;
    @JsonProperty(value = "base")
    private BigDecimal base;
    @JsonProperty(value = "rate")
    private BigDecimal rate;
    @JsonProperty(value = "gain")
    private BigDecimal gain;
    @JsonProperty(value = "deduction")
    private BigDecimal deduction;
    @JsonProperty(value = "total")
    @Builder.Default
    private boolean total = false;

    public boolean isZero() {
        return (gain == null || gain.doubleValue() == 0d) && (deduction == null || deduction.doubleValue() == 0d);
    }
}
