package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

import java.util.Collection;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipCloseDto {
    private Long templateId;
    private EmployeeDto employee;
    private PayslipAbsenceDto absences;
    private Collection<VariableSettingDto> variables;
}
