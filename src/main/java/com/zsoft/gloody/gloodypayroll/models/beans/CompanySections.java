package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.rest.dto.section.SectionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Getter
public class CompanySections {
    private final Map<String, SectionDto> sections;
    public Optional<SectionDto> findByCode (String code) {
        return Optional.ofNullable(sections.get(code));
    }
}

