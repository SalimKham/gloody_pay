package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BackPaymentDto implements Cloneable{
    private Long id;
    private String code;
    private String sectionCode;
    private Long contractId;
    private Long resourceId;
    private LocalDate startMonth;
    private LocalDate endMonth;
    private LocalDate month;
    private String title;
    private Double value;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
