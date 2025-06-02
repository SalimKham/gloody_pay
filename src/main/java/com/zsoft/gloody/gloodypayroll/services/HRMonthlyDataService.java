package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.HRMonthlySummaryDTO;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.HRSummaryMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.zsoft.gloody.gloodypayroll.utils.LogUtility.successMsg;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class HRMonthlyDataService extends CompanySupportService {
    public static final String MEAL_VOUCHER = "MEAL_VOUCHER";
    public static final String RESOURCE_MUTUAL = "RESOURCE_MUTUAL";
    public static final String TEMPLATE = "TEMPLATE";
    private final HRDataRepository hrDataRepository;
    private final HRSummaryMapper mapper;
    private final PayslipSettingRepository payslipSettingRepository;
    private final static List<String> CONTRACT_VARIABLES = List.of("DAYS_NBR_RCT", "CONTRACT_OFF_DAYS");
    private final static List<String> ABSENCE_VARIABLES = List.of("ABSENCE", "ABS");
    private final static List<String> EXPENSE_VARIABLES = List.of("EXP", "EXPENSE");
    private final static List<String> BONUS_VARIABLES = List.of("BNS", "BONUS");
    private final static List<String> ADVANCE_VARIABLES = List.of("ADVS", "ADVANCE");
    private final HRAsyncService asyncService;


    public void save(HRMonthlySummaryDTO summaryDTO) {
        if (summaryDTO.empty()) {
            return;
        }
        HRMonthlyData entity = mapper.toEntity(summaryDTO);
        entity.setMonth(summaryDTO.month().withDayOfMonth(1));
        hrDataRepository.saveAndFlush(entity);
        log.info(successMsg("HR VARIABLE SAVED: %s | CONTRACT: %s | VALUE: %s", summaryDTO.variable(), summaryDTO.contractId(), summaryDTO.value()));
    }

    public void save(List<HRMonthlySummaryDTO> summaryDTOS) {
        if (summaryDTOS == null || summaryDTOS.isEmpty()) {
            return;
        }
        HRMonthlySummaryDTO summaryDTO = summaryDTOS.get(0);
        boolean hasTemplate = hrDataRepository.hasTemplate(summaryDTO.contractId(), summaryDTO.month().withDayOfMonth(1));
        summaryDTOS = summaryDTOS.stream().filter(dto -> !dto.variable().equals(TEMPLATE) || !hasTemplate).toList();
        String toClearedVariables = getVariablesAsReg(summaryDTOS);
        hrDataRepository.deleteAllByVariableLikeAndContractIdInAndMonth(toClearedVariables, List.of(summaryDTO.contractId()), summaryDTO.month().withDayOfMonth(1));
        if (summaryDTO.empty()) {
            log.info(successMsg("HR VARIABLE CLEARED: %s | CONTRACT: %s | MONTH: %s", summaryDTO.variable(), summaryDTO.contractId(), summaryDTO.month().withDayOfMonth(1)));
        }
        Set<String> savedVariables = new HashSet<>(); // make sure
        summaryDTOS.forEach(hrMonthlySummaryDTO -> {
            if (!savedVariables.contains(hrMonthlySummaryDTO.variable()) ) {
                this.save(hrMonthlySummaryDTO);
                savedVariables.add(hrMonthlySummaryDTO.variable());
            }
        });
    }

    public int deleteByFieldMatchingRegex(LocalDate month, Long contactId, String regexPattern) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaDelete<HRMonthlyData> delete = cb.createCriteriaDelete(HRMonthlyData.class);
        Root<HRMonthlyData> root = delete.from(HRMonthlyData.class);

        Expression<Boolean> regexExpression = cb.function("REGEXP", Boolean.class,
                root.get("variable"),
                cb.literal(regexPattern));
        delete.where(
                cb.and(
                        cb.equal(root.get("month"), month),
                        cb.equal(root.get("contract"), contactId),
                        cb.isTrue(regexExpression)
                )
        );
        return entityManager.createQuery(delete).executeUpdate();
    }

    public List<Long> getMonthlyContractIds(Long resourceId, LocalDate date) {
        date = date.withDayOfMonth(1);
        return hrDataRepository.getContractIdsByMonthAndResourceIdIn(date, Set.of(resourceId)).stream().map(tuple -> tuple.get(1, Long.class)).toList();
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
        hrDataRepository.findTopHrMonthlyDataByVariableAndContractIdAndMonth("TEMPLATE", contractId, month.withDayOfMonth(1)).ifPresent(monthlyData -> payslipSettingRepository.deleteById((long) monthlyData.getValue()));
        hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("TEMPLATE", Collections.singleton(contractId), month);
        hrDataRepository.save(new HRMonthlyData(null, resourceId, contractId, month, "TEMPLATE", templateId));
        log.info(successMsg("TEMPLATE UPDATED | CONTRACT: %s | MONTH: %s", contractId, month));
        saveDrafts(Set.of(resourceId), month);
    }

    private String getVariablesAsReg(List<HRMonthlySummaryDTO> summaryDTOS) {
        AtomicReference<String> variables = new AtomicReference<>("-1");
        summaryDTOS.forEach(summaryDTO -> {
            List<String> dataVariables = getTobeClearedVariablesReg(summaryDTO.variable());
            if(dataVariables != null && !dataVariables.isEmpty()) {
                    variables.set(variables.get() + "|" + String.join("|", dataVariables));
            }
        });
        return variables.get();
    }
    private List<String> getTobeClearedVariablesReg(String variableName) {
        if (variableName == null) {
            return null;
        }
        List<String> variables = null;
        if (EXPENSE_VARIABLES.contains(variableName.split("_")[0])) {
            variables = EXPENSE_VARIABLES;
        } else if (BONUS_VARIABLES.contains(variableName.split("_")[0])) {
            variables = BONUS_VARIABLES;
        } else if (ADVANCE_VARIABLES.contains(variableName.split("_")[0])) {
            variables = ADVANCE_VARIABLES;
        } else if (MEAL_VOUCHER.contains(variableName.split("_")[0])) {
            variables = List.of(MEAL_VOUCHER);
        } else if (RESOURCE_MUTUAL.contains(variableName.split("_")[0])) {
            variables = List.of(RESOURCE_MUTUAL);
        } else if (TEMPLATE.contains(variableName.split("_")[0])) {
            variables = List.of(TEMPLATE);
        } else if (ABSENCE_VARIABLES.stream().anyMatch(variableName::contains)) {
            variables = ABSENCE_VARIABLES;
        } else if (CONTRACT_VARIABLES.stream().anyMatch(variableName::contains)) {
            variables = CONTRACT_VARIABLES;
        }

        return variables;
    }

}
