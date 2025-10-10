package com.zsoft.gloody.gloodypayroll.rest.dto.section;

import java.util.List;

public record SectionListFilter(
        String query,
        List<String> labels,
        List<Long> categories
) {
}