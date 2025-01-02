package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

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
    private boolean ratePercent;
    @Builder.Default
    private boolean total = false;

    public boolean isZero() {
        return (gain == null || gain.doubleValue() == 0d) && (deduction == null || deduction.doubleValue() == 0d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayslipElementDto that)) return false;
        return Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCode());
    }
}
