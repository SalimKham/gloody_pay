package com.zsoft.gloody.gloodypayroll.rest.mappers.accounting;

import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSettingGroup;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingGroupDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AccountingSettingAccountMapper.class})
public interface AccountingSettingGroupMapper{

    AccountingSettingGroupDTO toDto(AccountingSettingGroup AccountingSettingGroup);
    AccountingSettingGroup toEntity(AccountingSettingGroupDTO accountingSettingGroupDTO);
    default AccountingSettingGroup fromId(Long id) {
        if (id == null) {
            return null;
        }
        return AccountingSettingGroup.builder().id(id).build();
    }
}
