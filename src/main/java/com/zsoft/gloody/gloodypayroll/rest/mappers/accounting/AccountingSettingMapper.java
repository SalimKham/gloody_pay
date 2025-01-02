package com.zsoft.gloody.gloodypayroll.rest.mappers.accounting;

import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSetting;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingDTO;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {AccountingSettingGroupMapper.class})
public interface AccountingSettingMapper{

    AccountingSettingDTO toDto(AccountingSetting accountingSetting);
    @BeanMapping(builder = @Builder(disableBuilder = true))
    AccountingSetting toEntity(AccountingSettingDTO accountingSettingDTO);

    default AccountingSetting fromId(Long id) {
        if (id == null) {
            return null;
        }
        return AccountingSetting.builder().id(id).build();
    }

    @AfterMapping
    default void afterMapping(@MappingTarget AccountingSetting accountingSetting) {
        if (accountingSetting != null)  {
            accountingSetting.setCompany(SecurityUtils.getCurrentCompanyId());
        }
    }
}
