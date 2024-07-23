package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Getter
public class CompanySections {
    private final Map<String, Section> sections;
    public Optional<Section> findByCode (String code) {
        return Optional.ofNullable(sections.get(code));
    }
}

