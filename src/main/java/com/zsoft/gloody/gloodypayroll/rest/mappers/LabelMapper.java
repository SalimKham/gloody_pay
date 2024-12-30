package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.LabelSetting;
import com.zsoft.gloody.gloodypayroll.rest.dto.LabelSettingDto;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.mapstruct.*;

@Mapper
public interface LabelMapper {
    LabelSettingDto toDto(LabelSetting labelSetting);
    @BeanMapping(builder = @Builder(disableBuilder = true))
    LabelSetting toEntity(LabelSettingDto labelSettingDto);
    @AfterMapping
    default void afterMapping(@MappingTarget LabelSetting labelSetting) {
        if (labelSetting != null)  {
            labelSetting.setCompany(SecurityUtils.getCurrentCompanyId());
        }
    }
}


