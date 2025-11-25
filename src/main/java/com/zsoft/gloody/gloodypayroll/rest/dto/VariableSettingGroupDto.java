package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VariableSettingGroupDto {
    private Long id;
    private String label;
    private Boolean referenced;
}
