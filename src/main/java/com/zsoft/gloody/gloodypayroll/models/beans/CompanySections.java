package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import lombok.AllArgsConstructor;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class CompanySections {
    private final Map<String, Section> sections;
    public Optional<Section> findByCode (String code) {
        return Optional.ofNullable(sections.get(code));
    }
}

