package com.zsoft.gloody.gloodypayroll.services;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionCategory;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipConfigRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.SectionCategoryMapper;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class SectionsConfigService extends CompanySupportService {
   private final PayslipConfigRepository payslipConfigRepository;
   private final SectionCategoryMapper sectionCategoryMapper;
   private final CompanyDefaultConfigurationService companyDefaultConfigurationService;

    public void initCompanyDefaultConfiguration(List<Long> ids) {
        disableFilters();
        ids.forEach(id -> {
            if (payslipConfigRepository.findByCompany(id).isEmpty()) {
                companyDefaultConfigurationService.initCompanyConfiguration(id);
            }
        });
    }


    public Optional<List<SectionCategoryDto>> getByCompany(Long companyId) {
       return Optional.of(payslipConfigRepository.findByCompany(companyId)
               .stream()
               .map(sectionCategoryMapper::toDto).toList());
   }

    public Optional<List<SectionCategoryDto>> saveCategory(SectionCategoryDto category) {
       SectionCategory sectionCategory = payslipConfigRepository.findByIdAndCompany(category.getId(), SecurityUtils.getCurrentCompanyId()).orElse(SectionCategory.builder().sections(null).build());
       sectionCategory.setId(category.getId());
       sectionCategory.setCompany(category.getCompany());
       sectionCategory.setCode(category.getCode());
       sectionCategory.setName(category.getName());
       payslipConfigRepository.save(sectionCategory);
       return getByCompany(category.getCompany());
    }

    public Optional<List<SectionCategoryDto>> deleteCategory(Long id) {
        SectionCategory sectionCategory = payslipConfigRepository.findById(id).orElse(new SectionCategory());
        if (sectionCategory.getId() != null) {
            payslipConfigRepository.delete(sectionCategory);
            return getByCompany(sectionCategory.getCompany());
        }
        throw new RuntimeException("No such category");
    }

}
