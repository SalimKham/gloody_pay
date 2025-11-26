package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
    private String identifier;
    private BigDecimal base;
    private BigDecimal rate;
    private BigDecimal gain;
    private BigDecimal deduction;
    private boolean ratePercent;
    @Builder.Default
    private boolean total = false;
    private List<BackPaymentDto> backPayments;

    public boolean isZero() {
        return (gain == null || gain.doubleValue() == 0d) && (deduction == null || deduction.doubleValue() == 0d);
    }

    public Double getValueWithNoBackPayments() {
        if (backPayments == null || backPayments.isEmpty()) {
            return getValue();
        }
        return getValue() - backPayments.stream().map(BackPaymentDto::getValue).reduce(Double::sum).orElse(0d);

    }

    public Double getValue() {
        if (gain == null && deduction == null) {
            return 0d;
        }
        return Objects.requireNonNullElse(gain, deduction).doubleValue();
    }

    public BigDecimal getDeduction() {
       return  Objects.isNull(deduction) ? null :
        BigDecimal.valueOf(getValueWithNoBackPayments()).setScale(2, RoundingMode.HALF_UP) ;
    }

    public BigDecimal getGain() {
        return  Objects.isNull(gain) ? null :
                BigDecimal.valueOf(getValueWithNoBackPayments()).setScale(2, RoundingMode.HALF_UP) ;
    }


    public void setValue(Double value) {
        if (gain == null && deduction == null) {
            return;
        }
        if (Objects.nonNull(deduction)) {
            deduction = BigDecimal.valueOf(value);
            return;
        }
       gain =  BigDecimal.valueOf(value);
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
