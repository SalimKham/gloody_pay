package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.models.entities.SectionSetting;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class DefaultSectionSettings {
    private final Map<String, SectionSetting> sectionSettings;
    public Optional<SectionSetting> findByKey (String key) {
        return Optional.ofNullable(sectionSettings.get(key));
    }
}
