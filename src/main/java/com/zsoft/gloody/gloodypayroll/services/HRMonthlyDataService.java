package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.models.enums.SummaryDataType;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.HRMonthlySummaryDTO;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.HRSummaryMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zsoft.gloody.gloodypayroll.utils.LogUtility.successMsg;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class HRMonthlyDataService extends CompanySupportService {
    public static final String TEMPLATE = "TEMPLATE";
    private final HRDataRepository hrDataRepository;
    private final HRSummaryMapper mapper;
    private final PayslipSettingRepository payslipSettingRepository;
    private final HRAsyncService asyncService;


    public void save(List<HRMonthlySummaryDTO> summaryDTOS) {
        if (summaryDTOS == null || summaryDTOS.isEmpty()) {
            return;
        }
        HRMonthlySummaryDTO summaryDTO = summaryDTOS.get(0);
        LocalDate month = summaryDTO.month().withDayOfMonth(1);
        Set<Long> contractIds = summaryDTOS.stream().map(HRMonthlySummaryDTO::contractId).collect(Collectors.toSet());
        Set<SummaryDataType> sourceTypes = summaryDTOS.stream().map(HRMonthlySummaryDTO::type).collect(Collectors.toSet());
        if (contractIds.isEmpty() || sourceTypes.isEmpty())  {
            return;
        }
        hrDataRepository.deleteByContractIdInAndTypeInAndMonth(contractIds, sourceTypes, month);
        if (summaryDTO.empty()) {
            log.info(successMsg("HR VARIABLE CLEARED: %s | CONTRACT: %s | MONTH: %s", summaryDTO.variable(), summaryDTO.contractId(), summaryDTO.month().withDayOfMonth(1)));
        }
        Set<HRMonthlyData> data = summaryDTOS.stream().map(mapper::toEntity).peek(d -> d.setMonth(d.getMonth().withDayOfMonth(1))).collect(Collectors.toSet());
        hrDataRepository.saveAllAndFlush(data);
        log.info(successMsg("HR VARIABLES SAVED"));

    }


    public List<Long> getMonthlyContractIds(Long resourceId, LocalDate date) {
        date = date.withDayOfMonth(1);
        return hrDataRepository.getContractIdsByMonthAndResourceIdIn(date, Set.of(resourceId)).stream().map(tuple -> tuple.get(1, Long.class)).toList();
    }

    public List<Long> getAllMonthlyContracts(LocalDate date, Long companyId) {
        date = date.withDayOfMonth(1);
        return hrDataRepository.getAllMonthlyContracts(date, companyId);
    }

    public void deleteDrafts(Set<Long> resourceIds, LocalDate month) {
        Map<Long, Long> resourceContracts = hrDataRepository.getContractIdsByMonthAndResourceIdIn(month, resourceIds).stream()
                .collect(Collectors
                        .toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Long.class), (existing, replacement) -> existing));
        hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("DRAFT_ID", resourceContracts.values(), month);
        payslipSettingRepository.deleteAllByContractInAndPaymentDateAndDraftIsTrue(resourceContracts.values(), month);
    }

    public void saveDrafts(Set<Long> resourceIds, LocalDate month) {
        // todo uncomment this
       /* deleteDrafts(resourceIds, month);
        asyncService.saveDrafts(resourceIds, month, SecurityContextHolder.getContext().getAuthentication());*/
    }

    public void onTemplateChange(PayslipSettingDto template) {
        Set<Long> resourceIds = null;
        if (template.isTemplate() && template.getId() != null) {
            resourceIds = template.isByDefault() ? hrDataRepository.findAllResourcesByDefaultTemplate(LocalDate.now().withDayOfMonth(1)) :
                    hrDataRepository.findAllResourcesByTemplateId(LocalDate.now().withDayOfMonth(1), template.getId());
        }
        if (resourceIds != null && !resourceIds.isEmpty()) {
            saveDrafts(resourceIds, LocalDate.now().withDayOfMonth(1));
        }
    }

    public void updateHrTemplate(Long resourceId, Long contractId, LocalDate month, Long templateId) {
        hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("TEMPLATE", Collections.singleton(contractId), month);
        hrDataRepository.save(new HRMonthlyData(null, resourceId, contractId, SummaryDataType.TEMPLATE,  month, "TEMPLATE", templateId));
        log.info(successMsg("TEMPLATE UPDATED | CONTRACT: %s | MONTH: %s", contractId, month));
        saveDrafts(Set.of(resourceId), month);
    }



}
