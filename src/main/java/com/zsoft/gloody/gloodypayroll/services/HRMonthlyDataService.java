package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.HRMonthlySummaryDTO;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.HRSummaryMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class HRMonthlyDataService extends CompanySupportService {
    private final HRDataRepository hrDataRepository;
    private final HRSummaryMapper mapper;
    private final PayslipSettingRepository payslipSettingRepository;
    private final HRAsyncService asyncService;


    public void save(HRMonthlySummaryDTO summaryDTO) {
        HRMonthlyData entity = mapper.toEntity(summaryDTO);
        Long id = hrDataRepository
                .findHrMonthlyDataByVariableAndContractIdAndMonth(summaryDTO.variable(), summaryDTO.contractId(), summaryDTO.month())
                .map(HRMonthlyData::getId).orElse(null);
        entity.setId(id);
        hrDataRepository.save(entity);
    }

    public void save(List<HRMonthlySummaryDTO> summaryDTOS) {
        summaryDTOS.forEach(this::save);
    }

    public List<Long> getMonthlyContractIds(Long resourceId, LocalDate date) {
        date = date.withDayOfMonth(1);
        return hrDataRepository.getContractIdsByMonthAndResourceIdIn(date, Set.of(resourceId)).stream().map(tuple -> tuple.get(1, Long.class)).toList();
    }

    public void deleteDrafts(Set<Long> resourceIds, LocalDate month) {
        Map<Long, Long> resourceContracts = hrDataRepository.getContractIdsByMonthAndResourceIdIn(month, resourceIds).stream()
                .collect(Collectors
                        .toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Long.class),  (existing, replacement) -> existing));
        hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("DRAFT_ID", resourceContracts.values(), month);
        payslipSettingRepository.deleteAllByContractInAndPaymentDateAndDraftIsTrue(resourceContracts.values(), month);
    }

    public void saveDrafts(Set<Long> resourceIds, LocalDate month) {
        deleteDrafts(resourceIds, month);
        asyncService.saveDrafts(resourceIds, month, SecurityContextHolder.getContext().getAuthentication());
    }

    public void onTemplateChange(PayslipSettingDto template) {
        Set<Long> resourceIds = null;
        if (template.isTemplate() && template.getId() != null) {
            resourceIds = template.isByDefault() ?  hrDataRepository.findAllResourcesByDefaultTemplate(LocalDate.now().withDayOfMonth(1)):
                    hrDataRepository.findAllResourcesByTemplateId(LocalDate.now().withDayOfMonth(1), template.getId());
        }
        if (resourceIds != null && !resourceIds.isEmpty()) {
              saveDrafts(resourceIds, LocalDate.now().withDayOfMonth(1));
        }
    }

    public void updateHrTemplate(Long resourceId, Long contractId, LocalDate month, Long templateId) {
        Long variableId = hrDataRepository
                .findHrMonthlyDataByVariableAndContractIdAndMonth("TEMPLATE", contractId, month)
                .map(HRMonthlyData::getId).orElse(null);
        hrDataRepository.save(new HRMonthlyData(variableId, resourceId, contractId, month, "TEMPLATE", templateId));
        saveDrafts(Set.of(resourceId), month);
    }

}
