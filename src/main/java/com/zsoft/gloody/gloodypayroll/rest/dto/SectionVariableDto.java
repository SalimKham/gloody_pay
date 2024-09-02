package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SectionVariableDto {
    private Long id;
    private String variableKey;
    private String value;
}
