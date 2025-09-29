package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BackPaymentDto implements Cloneable{
    private Long id;
    private String code;
    private String title;
    private Double value;
    private Long elementId;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
