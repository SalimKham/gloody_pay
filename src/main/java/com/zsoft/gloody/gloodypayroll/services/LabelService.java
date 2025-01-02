package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.repositories.LabelSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.LabelSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.LabelMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class LabelService extends CompanySupportService {
    private final LabelSettingRepository labelSettingRepository;
    private final LabelMapper labelMapper;

    public Page<LabelSettingDto> findAll(Pageable pageable, String query) {
        query = query != null ? query : "";
        query = "%"+ query + "%";
        return labelSettingRepository.findAllByLabelLikeOrKeyLike(query, query ,  pageable).map(labelMapper::toDto);
    }

    public Page<LabelSettingDto> save(List<LabelSettingDto> variables, Pageable pageable,  String query) {
        labelSettingRepository.saveAll(variables.stream().map(labelMapper::toEntity).toList());
        return findAll(pageable, query);
    }

    public Page<LabelSettingDto> delete(Long labelId, Pageable pageable,  String query) {
        labelSettingRepository.deleteById(labelId);
        return findAll(pageable,  query);
    }
}
