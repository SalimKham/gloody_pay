package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import com.zsoft.gloody.gloodypayroll.rest.dto.CompanyDto;
import org.mapstruct.Mapper;

@Mapper(uses = {AddressMapper.class})
public interface CompanyMapper {
    Company toEntity(CompanyDto company);
    CompanyDto toDto(Company company);
    default Long toId(Company company) {
        return (company == null) ? null : company.getId();
    }

    default Company fromId(Long id) {
        return (id == null) ? null : Company.builder().id(id).build();
    }
}

