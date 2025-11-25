package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSettingGroup;
import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingGroupDto;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.mapstruct.*;

import java.util.Objects;

@Mapper
public interface VariableSettingGroupMapper {
    VariableSettingGroupDto toDto(VariableSettingGroup variableSettingGroup);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    VariableSettingGroup toEntity(VariableSettingGroupDto variableSettingGroupDto);

    @AfterMapping
     default void afterMapping(@MappingTarget VariableSettingGroup variableSettingGroup) {
        if (variableSettingGroup != null && Objects.isNull(variableSettingGroup.getCompany()))  {
            variableSettingGroup.setCompany(SecurityUtils.getCurrentCompanyId());
        }
    }
}


