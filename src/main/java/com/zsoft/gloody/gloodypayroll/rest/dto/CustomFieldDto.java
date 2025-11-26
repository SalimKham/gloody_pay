package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomFieldDto{
    private Long id;
    private String code;
    private Double value;
    private Long employeeId;
}
