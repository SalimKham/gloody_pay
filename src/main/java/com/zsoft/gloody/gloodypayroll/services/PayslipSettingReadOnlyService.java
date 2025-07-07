package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayslipSettingMapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PayslipSettingReadOnlyService extends CompanySupportService {
    private final PayslipSettingRepository payslipSettingRepository;
    private final PayslipSettingMapper payslipSettingMapper;
    private final HRDataRepository hrDataRepository;

    @Cacheable(
            cacheNames = "company_default_template",
            key = "T(com.zsoft.gloody.gloodypayroll.utils.SecurityUtils).getCurrentCompanyId()"
    )
    public Optional<PayslipSettingDto> findDefaultTemplate() {
        return this.payslipSettingRepository.findTopByByDefaultOrderByIdAsc(true).map(this::toDto);
    }

    public Optional<PayslipSettingDto> findById(Long id, Long employeeId, LocalDate date, Long contractId) {
        return this.payslipSettingRepository.findById(id).map(this::toDto);
    }

    @Cacheable(
            cacheNames = "company_template_by_id",
            key = "#id"
    )
    public Optional<PayslipSettingDto> findById(Long id) {
        return this.payslipSettingRepository.findById(id).map(this::toDto);
    }

    public Optional<PayslipSettingDto> findDraftById(Long id) {
        return this.payslipSettingRepository.findDraftPayslip(id).map(this::toDto);
    }

    public Page<PayslipSettingDto> findAllTemplates(String query, Pageable pageable) {
        query = query == null ? "%%" : "%" + query + "%";
        return this.payslipSettingRepository.findAllByNameLikeAndTemplateIsTrue( query, pageable)
                .map(this::toDto);
    }

    public PayslipSettingDto toDto(PayslipSetting payslipSetting) {
        PayslipSettingDto  payslipSettingDto = payslipSettingMapper.toDto(payslipSetting);
        if (payslipSettingDto != null && Objects.nonNull(payslipSettingDto.getElements())) {
            payslipSettingDto.getElements().forEach(payslipSettingElementDto -> {
                entityManager.detach(payslipSetting);
                payslipSettingElementDto.setPayslipSettingId(payslipSettingDto.getId());
            });
        }
        return payslipSettingDto;
    }


    private void updateHrTemplate(Long resourceId, Long contractId, LocalDate month, Long templateId) {
        Long variableId = hrDataRepository
                .findTopHrMonthlyDataByVariableAndContractIdAndMonth("TEMPLATE", contractId, month)
                .map(HRMonthlyData::getId).orElse(null);
        hrDataRepository.save(new HRMonthlyData(variableId, resourceId, contractId, month, "TEMPLATE", templateId));
    }


    public Map<Long, PayslipSettingDto> loadPayslipSettingsMap() {
        return payslipSettingRepository.findAllByDraftIsFalseAndClosedIsFalse().stream().map(this::toDto).collect(Collectors.toMap(PayslipSettingDto::getId, payslipSettingDto -> payslipSettingDto));
    }

}
