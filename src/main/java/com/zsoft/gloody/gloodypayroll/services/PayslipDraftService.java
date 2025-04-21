package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.RequestContext;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.HRMonthlySummaryDTO;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapper;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapperImpl;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayslipSettingMapper;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zsoft.gloody.gloodypayroll.services.PayslipService.calculatedElementToTemplateElement;

@Service
@RequiredArgsConstructor
@Transactional
public class PayslipDraftService extends EntityManagerService{
    private final EmployeeMapper employeeMapper;
    private final PayslipSettingMapper payslipSettingMapper;
    private final PayslipSettingRepository payslipSettingRepository;
    private final HRDataRepository hrDataRepository;

    @Async("draftAsyncExecutor")
    public void saveAsDraftAsync(Long companyId, PayslipSettingDto payslipDTO, TreeSet<PayslipElementDto> calculatedElements, Long contractId, Long resourceId, Employee employee, LocalDate month) {
        applyFilter(companyId);
        deleteAllDraftedByContractAndMonth(Set.of(contractId), month);
        save(payslipDTO, calculatedElements, contractId, resourceId, employee, month, false);
    }
    public PayslipSetting prepareDraft (PayslipSettingDto payslipDTO, Set<PayslipElementDto> calculatedElements, Long contractId, Long resourceId, Employee employee, LocalDate month, boolean clearDrafted) {
        try {
            payslipDTO = (PayslipSettingDto) payslipDTO.clone();
        } catch (Exception e) {
            return null;
        }

        month = month.withDayOfMonth(1);
        if (clearDrafted) {
            deleteAllDraftedByContractAndMonth(Set.of(contractId), month);
        }
        Map<String, PayslipElementDto> mappedElements = calculatedElements.stream().collect(Collectors.toMap(PayslipElementDto::getCode, elementDto -> elementDto));
        payslipDTO.getElements().forEach(payslipSettingElementDto -> {
            payslipSettingElementDto.setId(null);
            PayslipElementDto calculatedEl = mappedElements.get(payslipSettingElementDto.getSection());
            calculatedElementToTemplateElement(payslipSettingElementDto, calculatedEl);
        });
        payslipDTO.setDraft(true);
        payslipDTO.setId(null);
        payslipDTO.setTemplate(false);
        payslipDTO.setPaymentDate(month);
        payslipDTO.setPayslipDetail(employeeMapper.employeeToPayslipDetails(employee, contractId, month));
        PayslipSetting payslip = payslipSettingMapper.toEntity(payslipDTO);
        payslip.setContract(contractId);
        payslip.setEmployee(resourceId);
        return payslip;
    }

    public void save(PayslipSettingDto payslipDTO, Set<PayslipElementDto> calculatedElements, Long contractId, Long resourceId, Employee employee, LocalDate month, boolean clearDrafted) {
        PayslipSetting payslip = prepareDraft(payslipDTO, calculatedElements, contractId, resourceId, employee, month, clearDrafted);
        if (payslip == null) {
            return;
        }
        payslipSettingRepository.save(payslip);
        Long variableId = RequestContext.monthlyValues() != null ? null
                :  hrDataRepository.findHrMonthlyDataByVariableAndContractIdAndMonth("DRAFT_ID", contractId, month)
                .map(HRMonthlyData::getId).orElse(null);

        hrDataRepository.save(new HRMonthlyData(variableId , resourceId, contractId, month, "DRAFT_ID", payslip.getId()));
    }

    public void deleteAllDraftedByContractAndMonth(Set<Long> contractIds, LocalDate month) {
        hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("DRAFT_ID", contractIds, month);
        payslipSettingRepository.deleteAllByContractInAndPaymentDateAndDraftIsTrue(contractIds, month );
    }


}
