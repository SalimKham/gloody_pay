package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountingSettingGroupDTO  {
    private Long id;
    private String name;
    private List<AccountingSettingAccountDTO> accounts = null;
    private Double debitMonthlyTotal;
    private Double creditMonthlyTotal;
}
