package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.beans.DefaultSectionSettings;
import com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.SectionMapper;
import com.zsoft.gloody.gloodypayroll.utils.FieldAccessorUtils;
import com.zsoft.gloody.gloodypayroll.utils.SectionListUtils;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SectionService extends CompanySupportService {
    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final CacheService cacheService;
    private final SectionSettingRepository sectionSettingRepository;

    public Optional<SectionDto> findByCode(String code) {
        return this.sectionRepository.findByCodeAndCompany(code, SecurityUtils.getCurrentCompanyId())
                .map(this.sectionMapper::toDto);
    }

    public List<SectionDto> findAll() {
        return this.sectionRepository.findAll()
                .stream()
                .map(this.sectionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(
            cacheNames = "sectionMap",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public Map<String, SectionDto> findAllAsMap() {
        List<SectionMapEntry> sectionMap = sectionRepository.findAllByCompanyAsMap(SecurityUtils.getCurrentCompanyId());
       return sectionMap.stream().collect(Collectors.toMap(SectionMapEntry::getKey, sectionMapEntry -> sectionMapper.toDto(sectionMapEntry.getValue())));
    }

    public Page<SectionDto> findAllByPage(Pageable pageable) {
        Specification<Section> specification = Specification.where(((root, q, cb) -> q.getRestriction()));
        Pageable requestPageable = SectionListUtils.getPageable(pageable);
        Page<Section> page = this.sectionRepository.findAll(specification, requestPageable);
        List<SectionDto> content = page.stream()
                .map(this.sectionMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional
    public SectionDto create(SectionDto dto) {
        return this.save(dto);
    }


    @Transactional
    public SectionDto update(SectionDto dto) {
        if (dto.getCode() == null) {
            return null;
        }
        if (!this.sectionRepository.existsByCode(dto.getCode())) {
            return null;
        }
        return this.save(dto);
    }

    @Transactional
    public Optional<SectionDto> updateFields(String code, SectionDto dto, List<String> fields) {
        if (!this.sectionRepository.existsByCode(code)) {
            return Optional.empty();
        }
        return this.findByCode(code)
                .map(oldDto -> {
                    fields.forEach(fieldName -> {
                        Class<?> className = SectionDto.class;
                        Field field = FieldAccessorUtils.findField(className, fieldName);
                        if (field != null) {
                            FieldAccessorUtils.setFieldValue(className, field, oldDto, dto);
                        }
                    });
                    return oldDto;
                })
                .map(this::save);
    }

    @Transactional
    public boolean deleteById(String code) {
        if (this.sectionRepository.existsByCode(code)) {
            this.sectionRepository.deleteByCode(code);
            cacheService.updateSectionMapCache();
            return true;
        }
        return false;
    }

    public boolean notExistsById(String code) {
        return !this.sectionRepository.existsByCode(code);
    }

    @Transactional
    @CacheEvict( cacheNames = "sectionMap",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()")
    public SectionDto save(SectionDto dto) {
        try {
            Section section = this.sectionMapper.toEntity(dto);
            section = this.sectionRepository.save(section);
            cacheService.updateSectionMapCache();
            return this.sectionMapper.toDto(section);
        } catch (DataIntegrityViolationException e) {
            throw new ApiRuntimeException("error.section.code.already-exist");
        }
    }

    @Cacheable(
            cacheNames = "defaultSections",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public DefaultSectionSettings findDefaultSections() {
        DefaultSectionSettings defaultSectionSettings  =  new DefaultSectionSettings(sectionSettingRepository.findAllByCompany(SecurityUtils.getCurrentCompanyId()).stream().collect(Collectors.toMap(SectionSetting::getKey, sectionSetting -> sectionSetting)));
        return defaultSectionSettings;
    }
}
