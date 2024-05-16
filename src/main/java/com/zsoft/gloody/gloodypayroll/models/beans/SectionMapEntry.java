package com.zsoft.gloody.gloodypayroll.models.beans;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;

import java.util.AbstractMap;

public class SectionMapEntry extends AbstractMap.SimpleEntry<String, Section> {
    public SectionMapEntry(String key, Section value) {
        super(key, value);
    }
}
