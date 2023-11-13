package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import org.mapstruct.Mapper;

@Mapper
public interface CompanyMapper {
    default Long toId(Company company) {
        return (company == null) ? null : company.getId();
    }

    default Company fromId(Long id) {
        return (id == null) ? null : Company.builder().id(id).build();
    }
}

