package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSetting;
import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingDto;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface VariableMapper {
    VariableSettingDto toDto(VariableSetting variableSetting);
    VariableSetting toEntity(VariableSettingDto variableSettingDto);

    @AfterMapping
     default void afterMapping(@MappingTarget VariableSetting variable) {
        if (variable != null && variable.getCompany() == null)  {
            variable.setCompany(SecurityUtils.getCurrentCompanyId());
        }
    }
}


