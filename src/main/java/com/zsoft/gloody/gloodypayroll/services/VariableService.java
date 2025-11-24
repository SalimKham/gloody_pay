package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.VariableSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.VariableMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Transactional
public class VariableService extends CompanySupportService {
    private final VariableSettingRepository variableRepository;
    private final VariableMapper variableMapper;

    public Page<VariableSettingDto> findAll(Pageable pageable, String query) {
        query = query != null ? query : "";
        query = "%" + query + "%";
        return variableRepository.findAllByLabelLikeOrKeyLike(query, query, pageable).map(variableMapper::toDto);
    }

    public Page<VariableSettingDto> findAll(Pageable pageable, VariableSettingFilter filter) {
        String finalQuery = "%" + Objects.requireNonNullElse(filter.query(), "") + "%";
        Specification<VariableSetting> specification = Specification.where((root, query, cb) ->  {
            query.distinct(true);
           return cb.and();
        });
        specification = specification.and((root, query, cb) -> cb.or( cb.like(root.get("label"), "%" + finalQuery + "%"), cb.like(root.get("key"), "%" + finalQuery + "%")) );
        if(Objects.nonNull(filter.groupIds()) && !filter.groupIds().isEmpty()) {
            specification = specification.and((root, query, cb) ->  root.get("group").get("id").in(filter.groupIds()));
        }
        return variableRepository.findAll(specification, pageable).map(variableMapper::toDto);
    }


    @Cacheable(
            cacheNames = "company_variables",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public List<VariableSettingDto> findAllUnsourcedVariables() {
        return variableRepository.findAll().stream().map(variableMapper::toDto).toList();
    }

    private void save(VariableSettingDto variable) {
        if (Objects.isNull(variable.getId())) {
            if (variableRepository.existsByKeyAndCompany(variable.getKey(), variable.getCompany())) {
                variable.setKey(variable.getFallBackKey());
            }
            if (variableRepository.existsByKeyAndCompany(variable.getKey(), variable.getCompany())) {
                return;
            }
            if (Objects.nonNull(variable.getSource())) {
                variableRepository.findTopBySource(variable.getSource()).ifPresent(v -> {
                    variable.setId(v.getId());
                    variable.setKey(v.getKey());
                });
            }
        } else {
            if(variableRepository.existsByKeyAndCompanyAndIdNot(variable.getKey(), variable.getCompany(), variable.getId())) {
                return;
            }
        }
        variableRepository.save(variableMapper.toEntity(variable));
    }


    @CacheEvict(
            cacheNames = "company_variables",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public Page<VariableSettingDto> save(List<VariableSettingDto> variables, Pageable pageable, String query) {
        variables.forEach(this::save);
        return findAll(pageable, query);
    }

    public Page<VariableSettingDto> delete(Long variableId, Pageable pageable, String query) {
        variableRepository.deleteById(variableId);
        return findAll(pageable, query);
    }

    public void delete(String source) {
        variableRepository.deleteBySource(source);
    }

    public void deleteAllSourcedForCompany(Long companyId) {
        variableRepository.deleteAllBySourceIsNotNullAndCompany(companyId);
    }
}
