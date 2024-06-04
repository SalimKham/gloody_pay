package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.VariableSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.VariableMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class VariableService extends CompanySupportService {
    private final VariableSettingRepository variableRepository;
    private final VariableMapper variableMapper;

    public Page<VariableSettingDto> findAll(Pageable pageable) {
        return variableRepository.findAll(pageable).map(variableMapper::toDto);
    }

    public Page<VariableSettingDto> save(List<VariableSettingDto> variables, Pageable pageable) {
        variableRepository.saveAll(variables.stream().map(variableMapper::toEntity).toList());
        return findAll(pageable);
    }

    public Page<VariableSettingDto> delete(Long variableId, Pageable pageable) {
        variableRepository.deleteById(variableId);
        return findAll(pageable);
    }

    public void delete(String source) {
        variableRepository.deleteBySource(source);
    }
}
