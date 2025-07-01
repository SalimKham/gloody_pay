package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayslipSettingMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class PayslipSettingService extends CompanySupportService {
    private final PayslipSettingRepository payslipSettingRepository;
    private final PayslipSettingMapper payslipSettingMapper;
    private final PayslipSettingReadOnlyService payslipSettingReadOnlyService;
    private final HRMonthlyDataService hrMonthlyDataService;

    public Optional<PayslipSettingDto> markAsDefault(Long templateId) {
        payslipSettingRepository.markAsDefault(templateId);
        return payslipSettingReadOnlyService.findById(templateId);
    }

    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "company_template_by_id",
                    key = "#result.id"
            ),
            @CacheEvict(
                    cacheNames = "company_default_template",
                    key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
            )
    })
    public PayslipSettingDto save(PayslipSettingDto dto, Long resourceId) {
        try {
            PayslipSetting payslipSetting = this.payslipSettingMapper.toEntity(dto);
            boolean updateEmployeeMonthlyTemplate = false;
            if (dto.isTemplate()) {
                payslipSetting.setPaymentDate(null);
            } else if(payslipSetting.getPaymentDate()!= null) {
                payslipSetting.setPaymentDate(payslipSetting.getPaymentDate().withDayOfMonth(1));
                dto.setPaymentDate(dto.getPaymentDate().withDayOfMonth(1));
            }
            if (resourceId != null && Objects.nonNull(dto.getPaymentDate()) && Objects.nonNull(dto.getContract())) {
                payslipSettingRepository.deleteByContractAndPaymentDate(dto.getContract().getId(), payslipSetting.getPaymentDate());
                payslipSetting.setId(null);
                payslipSetting.setEmployee(resourceId);
                payslipSetting.setPayslipDetail(null);
                updateEmployeeMonthlyTemplate = true;
            }
            payslipSetting = this.payslipSettingRepository.save(payslipSetting);
            PayslipSettingDto result = this.payslipSettingMapper.toDto(payslipSetting);
            if (updateEmployeeMonthlyTemplate) {
                hrMonthlyDataService.updateHrTemplate(resourceId, dto.getContract().getId(), dto.getPaymentDate().withDayOfMonth(1), payslipSetting.getId());
            } else {
                hrMonthlyDataService.onTemplateChange(result);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Page<PayslipSettingDto> deleteTemplate(Long templateId, Pageable pageable) {
        payslipSettingRepository.deleteById(templateId);
        return payslipSettingReadOnlyService.findAllTemplates("", pageable);
    }

}
