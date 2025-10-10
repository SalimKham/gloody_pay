package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.SectionCategory;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(uses = SectionMapper.class)
public abstract class SectionCategoryMapper {

    public abstract SectionCategoryDto toDto(SectionCategory sectionCategory);

    public abstract SectionCategory toEntity(SectionCategoryDto sectionCategoryDto);

    @Named("toMinimalSectionCategory")
    public abstract SectionCategoryDto toMinimal(SectionCategory sectionCategory);
}
