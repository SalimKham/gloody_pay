package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.models.entities.SectionSetting;
import java.util.Map;
import java.util.Optional;

public record DefaultSectionSettings(Map<String, SectionSetting> sectionSettings) {
    public Optional<SectionSetting> findByKey (String key) {
        return Optional.ofNullable(sectionSettings.get(key));
    }
}
