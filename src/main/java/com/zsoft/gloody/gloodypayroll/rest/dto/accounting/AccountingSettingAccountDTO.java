package com.zsoft.gloody.gloodypayroll.rest.dto.accounting;

import com.zsoft.gloody.gloodypayroll.models.enums.AccountingSettingAccountType;
import lombok.*;

import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountingSettingAccountDTO implements Cloneable {
    private Long id;
    private String code;
    private String title;
    private String employee;
    private Boolean detailedByEmployee;
    private AccountingSettingAccountType type;
    private Double monthlyTotal;
    private Boolean referenced;
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountingSettingAccountDTO accountingSettingAccountDTO = (AccountingSettingAccountDTO) o;
        if (accountingSettingAccountDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), accountingSettingAccountDTO.getId());
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
