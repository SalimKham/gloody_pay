package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSettingGroup;
import com.zsoft.gloody.gloodypayroll.models.repositories.VariableSettingGroupRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingGroupDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.VariableSettingGroupMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class VariableSettingGroupService extends CompanySupportService {
    private final VariableSettingGroupRepository repository;
    private final VariableSettingGroupMapper mapper;

    public Page<VariableSettingGroupDto> findAll(Pageable pageable, String query) {
        query = query != null ? query : "";
        String finalQuery = "%" + query + "%";
        Specification<VariableSettingGroup> specification = Specification.where((root, cq, cb) -> {
            cq.distinct(true);
            return cb.like(root.get("label"), finalQuery);
        } );
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    public void save(VariableSettingGroupDto variableSettingGroupDto) {
       repository.save(mapper.toEntity(variableSettingGroupDto));
    }


    public void delete(Long variableId) {
        repository.deleteById(variableId);
    }
}
