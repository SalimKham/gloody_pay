package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VariableSettingDto {
    private Long company;
    private Long id;
    private String key;
    private String source;
    private String formula;
    private String label;
    private String description;
}
