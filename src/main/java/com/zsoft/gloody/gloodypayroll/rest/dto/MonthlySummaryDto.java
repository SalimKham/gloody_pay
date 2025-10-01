package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingAccountDTO;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MonthlySummaryDto {
    private AccountingSettingAccountDTO account;
    private String code;
    private String label;
    private Double base;
    private Double rate;
    private Double gain;
    private Double deduction;
    private int numberOfEmployees;
}
