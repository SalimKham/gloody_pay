package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionCategory;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.accounting.AccountingSettingAccountMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import static com.zsoft.gloody.gloodypayroll.configs.RequestContext.companySections;

@Mapper(uses = AccountingSettingAccountMapper.class)
public abstract class SectionMapper {

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
                .id(sectionCategory.getId())
                .name(sectionCategory.getName())
                .code(sectionCategory.getCode())
                .company(sectionCategory.getCompany())
                .build();
    }

    public Section fromCode(String code) {
        if (code == null) {
            return null;
        }
        return companySections().findByCode(code).map(section -> Section.builder()
                .id(section.getId()).code(section.getCode()).build()).orElse(null);
    }
}
