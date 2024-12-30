package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import lombok.*;

import java.util.Collections;
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
    private List<AccountingSettingAccountDTO> accounts = Collections.emptyList();
    private Double debitMonthlyTotal;
    private Double creditMonthlyTotal;
}
