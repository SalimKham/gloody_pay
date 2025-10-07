package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.beans.SectionMapEntry;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.section.SectionDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.section.SectionListFilter;
import com.zsoft.gloody.gloodypayroll.rest.mappers.SectionMapper;
import com.zsoft.gloody.gloodypayroll.support.StringListConverter;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SectionService extends CompanySupportService {
    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final CacheService cacheService;

    public Optional<SectionDto> findByCode(String code) {
        return this.sectionRepository.findByCodeAndCompany(code, SecurityUtils.getCurrentCompanyId())
                .map(this.sectionMapper::toDto);
    }

    public Boolean existByIdentifier(Integer identifier) {
        return this.sectionRepository.existsByIdentifierAndCompany(identifier, SecurityUtils.getCurrentCompanyId());
    }

    public Integer getNextIdentifier() {
        return this.sectionRepository.findTopByCompanyOrderByIdentifierDesc(SecurityUtils.getCurrentCompanyId()).map(section -> section.getIdentifier() + 1).orElse(9999);
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

    public List<SectionDto> findAll(SectionListFilter filter) {
        Specification<Section> specification = Specification.where(((root, q, cb) -> {
            q.distinct(true);
            return cb.and();
        }));
        if (Objects.nonNull(filter.query())) {
            specification = specification.and((root, q, cb) -> cb.like(cb.lower(cb.concat(root.get("identifier"), root.get("label"))), "%" + filter.query().toLowerCase() + "%"));
        }
        if (Objects.nonNull(filter.categories()) && !filter.categories().isEmpty()) {
            specification = specification.and((root, query, cb) -> root.get("category").get("id").in(filter.categories()));
        }
        if (Objects.nonNull(filter.labels()) && !filter.labels().isEmpty()) {
            specification = specification.and((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                for (String label : filter.labels()) {
                    predicates.add(cb.like(
                                    cb.concat(root.get("labelKeys"), StringListConverter.SPLIT_CHAR),
                                    "%" + label + StringListConverter.SPLIT_CHAR + "%"
                            )
                    );
                }
                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }
        List<Section> result = this.sectionRepository.findAll(specification);
        return result.stream()
                .map(this.sectionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SectionDto> create(SectionDto dto) {
        return this.save(dto);
    }


    @Transactional
    public List<SectionDto> update(SectionDto dto) {
        if (dto.getCode() == null) {
            return null;
        }
        if (!this.sectionRepository.existsByCode(dto.getCode())) {
            return null;
        }
        return this.save(dto);
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
    @CacheEvict(cacheNames = "sectionMap",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()")
    public List<SectionDto> save(SectionDto dto) {
        try {
            Section section = this.sectionMapper.toEntity(dto);
            if (section.getIdentifier() == null) {
                section.setIdentifier(getNextIdentifier());
            }
            section = this.sectionRepository.save(section);
            cacheService.updateSectionMapCache();
            return this.findAll();
        } catch (DataIntegrityViolationException e) {
            throw new ApiRuntimeException("error.section.code.already-exist");
        }
    }

}
