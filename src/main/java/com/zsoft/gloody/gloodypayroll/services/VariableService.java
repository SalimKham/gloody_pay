package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.repositories.VariableSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.VariableMapper;
import lombok.AllArgsConstructor;
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
        query = "%"+ query + "%";
        return variableRepository.findAllByLabelLikeOrKeyLike(query, query ,pageable).map(variableMapper::toDto);
    }

    public Page<VariableSettingDto> save(List<VariableSettingDto> variables, Pageable pageable, String query) {
        variableRepository.saveAll(variables.stream().filter(variableSettingDto -> {
            boolean existsBySource = false;
            if (Objects.nonNull(variableSettingDto.getSource())) {
                existsBySource = variableRepository.existsBySourceAndCompany(variableSettingDto.getSource(), variableSettingDto.getCompany());
            }
            return !existsBySource && !variableRepository.existsByKeyAndSourceAndCompany(variableSettingDto.getKey(), variableSettingDto.getSource(),variableSettingDto.getCompany());
        })
                .map(variableMapper::toEntity).toList());
        return findAll(pageable, query);
    }

    public Page<VariableSettingDto> delete(Long variableId,  Pageable pageable, String query) {
        variableRepository.deleteById(variableId);
        return findAll(pageable, query);
    }

    public void delete(String source) {
        variableRepository.deleteBySource(source);
    }
}
