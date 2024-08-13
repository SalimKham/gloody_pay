package com.zsoft.gloody.gloodypayroll.models.beans;

import java.util.AbstractMap;

public class SectionIdMapEntry extends AbstractMap.SimpleEntry<Long, String> {
    public SectionIdMapEntry(Long key, String code) {
        super(key, code);
    }
}
