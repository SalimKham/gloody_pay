package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.repositories.VariableSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.VariableMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @Cacheable(
            cacheNames = "company_variables",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public List<VariableSettingDto> findAllUnsourcedVariables() {
        return variableRepository.findAll().stream().map(variableMapper::toDto).toList();
    }


    @CacheEvict(
            cacheNames = "company_variables",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public Page<VariableSettingDto> save(List<VariableSettingDto> variables, Pageable pageable, String query) {
        variableRepository.saveAll(variables.stream().filter(variableSettingDto -> !variableRepository.existsByKeyAndCompany(variableSettingDto.getKey(), variableSettingDto.getCompany()))
                .peek(variableSettingDto -> {
                    if (Objects.nonNull(variableSettingDto.getSource())) {
                        variableRepository.findTopBySource(variableSettingDto.getSource()).ifPresent(v -> variableSettingDto.setId(v.getId()));
                    }
                })
                .map(variableMapper::toEntity).toList());
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
