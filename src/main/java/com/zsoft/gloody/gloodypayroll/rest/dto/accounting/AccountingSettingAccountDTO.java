package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import com.zsoft.gloody.gloodypayroll.models.enums.AccountingSettingAccountType;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountingSettingAccountDTO {
    private Long id;
    private String code;
    private String title;
    private Boolean detailedByEmployee;
    private AccountingSettingAccountType type;
    private Double monthlyTotal;
    private Boolean referenced;
}
