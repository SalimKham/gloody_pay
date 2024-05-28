package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionCategory;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper
public abstract class SectionMapper {
    @Autowired
    private SectionRepository sectionRepository;

    @Mapping(target = "category", source = "category", qualifiedByName = "toMinimalSectionCategory")
    public abstract SectionDto toDto(Section section);

    public abstract Section toEntity(SectionDto sectionDto);

    public String toId(Section section) {
        return (section == null) ? null : section.getCode();
    }

    @Named("toMinimalSectionCategory")
    public SectionCategoryDto toMinimalSectionCategory(SectionCategory sectionCategory){
        if (sectionCategory == null) {
            return null;
        }
        return SectionCategoryDto.builder()
                .name(sectionCategory.getName())
                .code(sectionCategory.getCode())
                .company(sectionCategory.getCompany())
                .build();
    }

    public Section fromCode(String code) {
        if (code == null) {
            return null;
        }
        return sectionRepository.findByCode(code).map(section -> Section.builder()
                .id(section.getId()).code(section.getCode()).build()).orElse(null);
    }
}
