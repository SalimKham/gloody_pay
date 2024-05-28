package com.zsoft.gloody.gloodypayroll.services;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionCategory;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipConfigRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.SectionCategoryDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.SectionCategoryMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SectionsConfigService extends TenantSupportService {
   private final PayslipConfigRepository payslipConfigRepository;
   private final SectionCategoryMapper sectionCategoryMapper;

   public Optional<List<SectionCategoryDto>> getByCompany(Long companyId) {
       return Optional.of(payslipConfigRepository.findByCompany(companyId)
               .stream()
               .map(sectionCategoryMapper::toDto).toList());
   }

    public Optional<List<SectionCategoryDto>> saveCategory(SectionCategoryDto category) {
       SectionCategory sectionCategory = payslipConfigRepository.findById(category.getCode()).orElse(new SectionCategory());
       sectionCategory.setCompany(category.getCompany());
       sectionCategory.setCode(category.getCode());
       sectionCategory.setName(category.getName());
       payslipConfigRepository.save(sectionCategory);
       return getByCompany(category.getCompany());
    }

    public Optional<List<SectionCategoryDto>> deleteCategory(String code) {
        SectionCategory sectionCategory = payslipConfigRepository.findById(code).orElse(new SectionCategory());
        if (sectionCategory != null) {
            payslipConfigRepository.delete(sectionCategory);
            return getByCompany(sectionCategory.getCompany());
        }
        throw new RuntimeException("No such category");
    }

}
