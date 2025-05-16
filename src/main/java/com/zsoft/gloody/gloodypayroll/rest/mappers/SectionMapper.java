package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionCategory;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.accounting.AccountingSettingAccountMapper;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(uses = AccountingSettingAccountMapper.class)
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
       return  sectionRepository.findByCodeAndCompany(code, SecurityUtils.getCurrentCompanyId()).map(section -> Section.builder().id(section.getId()).code(section.getCode()).build()).orElse(null);
    }
}
