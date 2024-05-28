package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipSettingElementDto {
    private Long id;
    private Long payslipSettingId;
    private String label;
    private String section;
    private Double value;
    private Double baseValue;
    private Double rateValue;
    private boolean fix;
    private int elementOrder;
}
