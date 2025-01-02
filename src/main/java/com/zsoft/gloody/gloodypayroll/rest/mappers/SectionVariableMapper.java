package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.SectionVariable;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionVariableDto;
import org.mapstruct.Mapper;

@Mapper
public interface SectionVariableMapper {
    SectionVariableDto toDto(SectionVariable sectionVariable);
    SectionVariable toEntity(SectionVariableDto sectionVariableDto);

}


