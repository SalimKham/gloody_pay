package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.SectionMapper;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CacheService {

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;

    @CachePut(
            cacheNames = "sectionMap",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public Map<String, SectionDto> updateSectionMapCache() {
        List<SectionMapEntry> sectionMap = sectionRepository.findAllByCompanyAsMap(SecurityUtils.getCurrentCompanyId());
        return sectionMap.stream().collect(Collectors.toMap(SectionMapEntry::getKey, sectionMapEntry -> sectionMapper.toDto(sectionMapEntry.getValue())));
    }

}
