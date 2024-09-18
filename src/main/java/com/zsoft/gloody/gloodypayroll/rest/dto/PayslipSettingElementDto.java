package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipSettingElementDto implements Cloneable{
    private Long id;
    private Long payslipSettingId;
    private String label;
    private String section;
    private Double value;
    private Double baseValue;
    private Double rateValue;
    private boolean fix;
    private int elementOrder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayslipSettingElementDto that)) return false;
        return Objects.equals(getSection(), that.getSection());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSection());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
