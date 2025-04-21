package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import com.zsoft.gloody.gloodypayroll.models.repositories.CompanyRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.CompanyDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.CompanyMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@AllArgsConstructor
@Transactional
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    public void save(CompanyDto companyDto) {
        Company entity = companyMapper.toEntity(companyDto);
        Company company = companyRepository
                .findById(companyDto.getId())
                .orElse(null);
        if (Objects.nonNull(company)) {
            if (companyDto.getAddress()!= null && company.getAddress()!=null) {
                entity.getAddress().setId(company.getAddress().getId());
            }
        }
        companyRepository.save(entity);
    }
}
