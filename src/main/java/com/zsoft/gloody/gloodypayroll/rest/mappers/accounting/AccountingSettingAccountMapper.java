package com.zsoft.gloody.gloodypayroll.rest.mappers.accounting;

import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSettingAccount;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingAccountDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountingSettingAccountMapper  {

    AccountingSettingAccountDTO toDto(AccountingSettingAccount accountingSettingAccount);

    AccountingSettingAccount toEntity(AccountingSettingAccountDTO accountingSettingAccountDTO);

    default AccountingSettingAccount fromId(Long id) {
        if (id == null) {
            return null;
        }
        return AccountingSettingAccount.builder().id(id).build();
    }

}
