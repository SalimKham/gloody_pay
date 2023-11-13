package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionDto;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper
public abstract class SectionMapper {
    @Autowired
    private SectionRepository sectionRepository;

    public abstract SectionDto toDto(Section section);

    public abstract Section toEntity(SectionDto sectionDto);

    public String toId(Section section) {
        return (section == null) ? null : section.getCode();
    }

    public Section fromCode(String code) {
        if (code == null) {
            return null;
        }
        return sectionRepository.findByCode(code).orElse(null);
    }
}
