package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipElementDto {
    private String code;
    private String label;
    private String description;
    private BigDecimal base;
    private BigDecimal rate;
    private BigDecimal gain;
    private BigDecimal deduction;
    @Builder.Default
    private boolean total = false;

    public boolean isZero() {
        return (gain == null || gain.doubleValue() == 0d) && (deduction == null || deduction.doubleValue() == 0d);
    }
}
