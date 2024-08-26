package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountingSettingGroupDTO  {
    private Long id;
    private String name;
    private Set<AccountingSettingAccountDTO> accounts = new HashSet<>();
    private Double debitMonthlyTotal;
    private Double creditMonthlyTotal;
}
