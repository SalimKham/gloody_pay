package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.CustomField;
import com.zsoft.gloody.gloodypayroll.rest.dto.CustomFieldDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper()
public abstract class CustomFieldMapper {


    @Mapping(target = "employeeId", source = "employee.id")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract CustomFieldDto toDto(CustomField customField);

    @Mapping(target = "employee", ignore = true)
    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract CustomField toEntity(CustomFieldDto customFieldDto);
}

