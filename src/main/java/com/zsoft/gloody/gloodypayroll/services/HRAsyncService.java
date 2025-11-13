package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.RequestContext;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipCalculatePayload;
import com.zsoft.gloody.gloodypayroll.utils.CommonUtils;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HRAsyncService extends EntityManagerService {
    private final EmployeeRepository employeeRepository;
    private final PayslipDraftService payslipDraftService;
    private final PayslipService payslipService;
    private final HRDataRepository hrDataRepository;
    private final PayslipSettingRepository payslipSettingRepository;
    private final int PAGE_SIZE = 1000;

    @Async("draftAsyncExecutor")
    public void saveDrafts(Set<Long> resourceIds, LocalDate month, Authentication authentication) {
        try {
            if (resourceIds == null) {
                return;
            }
            month = month.withDayOfMonth(1);
            Set<Long> closedIds = payslipSettingRepository.getAllClosedOrDraftPayslipResources(month);
            resourceIds.removeAll(closedIds);
            if (resourceIds.isEmpty()) {
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            applyFilter(SecurityUtils.getCurrentCompanyId());
            RequestContext.initDefault();
            RequestContext.initTemplates();
            Map<Long, Long> resourceContracts = hrDataRepository.getContractIdsByMonthAndResourceIdIn(month, resourceIds).stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Long.class), (existing, replacement) -> existing));
            payslipDraftService.deleteAllDraftedByContractAndMonth(new HashSet<>(resourceContracts.values()), month);
            List<Set<Long>> resourcesSplit = CommonUtils.splitSet(resourceIds, PAGE_SIZE);
            for (Set<Long> resources : resourcesSplit) {
                saveDraftsForEmployees(month, resources, resourceContracts);
            }
        } catch (Exception e) {
            log.error("Error saving drafts: " + e.getMessage());
            e.printStackTrace();
        } finally {
            RequestContext.clear();
        }

    }



    public void saveDraftsForEmployees(LocalDate month, Set<Long> resourceIds, Map<Long, Long> resourceContracts) {
        PayslipCalculatePayload payload = new PayslipCalculatePayload();
        payload.setMonth(month);
        Map<Long, Employee> employeeMap = employeeRepository.findAllById(resourceIds).stream().collect(Collectors.toMap(Employee::getId, em -> em));
        AtomicInteger i = new AtomicInteger();
        log.info("DRAFTING PAYSLIPS STARTED FOR {}", resourceIds.size());
        long start = System.currentTimeMillis();
        LocalDate finalMonth = month;
        ArrayList<PayslipSetting> payslipSettings = new ArrayList<>();
        RequestContext.initMonthlyValues(month, resourceIds);
        payslipService.initFunctionElements(resourceIds);
        resourceContracts.keySet().forEach(resourceId -> {
            if (!resourceIds.contains(resourceId)) {
                return;
            }
            Long contractId = resourceContracts.get(resourceId);
            payload.setContractId(contractId);
            payload.setResourceId(resourceId);
            PayslipController.CalculatedPayslip calculatedPayslip = payslipService.calculate(payload, employeeMap.get(resourceId),  /*This must be false*/ false);
            PayslipSetting payslipSetting = payslipDraftService.prepareDraft(calculatedPayslip.template(), calculatedPayslip.calculatedElements(), contractId, resourceId, employeeMap.get(resourceId), finalMonth);
            payslipSettings.add(payslipSetting);
            i.getAndIncrement();
        });
        if (!payslipSettings.isEmpty()) {
            List<PayslipSetting> savedDrafts = payslipSettingRepository.saveAll(payslipSettings);
            hrDataRepository.bulkInsertDraftIds(month, savedDrafts.stream().map(PayslipSetting::getId).collect(Collectors.toList()));
        }
        long end = System.currentTimeMillis();
        log.info(" {} OF RESOURCES UPDATED IN {} ms ", resourceIds.size(), (end - start));
    }
}
