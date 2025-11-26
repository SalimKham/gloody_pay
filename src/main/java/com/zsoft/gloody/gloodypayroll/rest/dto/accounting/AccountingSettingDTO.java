package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import lombok.*;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountingSettingDTO  {
    private Long id;
    private Set<AccountingSettingGroupDTO> groups = null;
    private Double debitMonthlyTotal;
    private Double creditMonthlyTotal;
}
