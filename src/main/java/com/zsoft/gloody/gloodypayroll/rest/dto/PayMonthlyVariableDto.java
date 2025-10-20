package com.zsoft.gloody.gloodypayroll.rest.dto;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayMonthlyVariableDto {
    private Long id;
    private String key;
    private String label;
    private Double value;
}
