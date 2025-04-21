package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.RequestContext;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.HRMonthlySummaryDTO;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
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
public class HRAsyncService extends EntityManagerService {

    private final EmployeeRepository employeeRepository;
    private final PayslipDraftService payslipDraftService;
    private final PayslipService payslipService;
    private final HRDataRepository hrDataRepository;
    private final PayslipSettingRepository payslipSettingRepository;

    @Async("draftAsyncExecutor")
    public void saveDrafts(Set<Long> resourceIds, LocalDate month, Authentication authentication) {
       try {
           if (resourceIds == null || resourceIds.isEmpty()) {return;}
           month = month.withDayOfMonth(1);
           SecurityContextHolder.getContext().setAuthentication(authentication);
           applyFilter(SecurityUtils.getCurrentCompanyId());
           RequestContext.initDefault();
           RequestContext.initTemplates();
           RequestContext.initMonthlyValues(resourceIds);
           Map<Long, Long>  resourceContracts = hrDataRepository.getContractIdsByMonthAndResourceIdIn(month, resourceIds).stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Long.class),  (existing, replacement) -> existing));
           PayslipController.PayslipCalculatePayload payload = new PayslipController.PayslipCalculatePayload();
           payload.setMonth(month.withDayOfMonth(1));
           Map<Long, Employee> employeeMap = employeeRepository.findAllById(resourceIds).stream().collect(Collectors.toMap(Employee::getId, em -> em));
           AtomicInteger i = new AtomicInteger();
           System.out.println("DRAFTING PAYSLIPS STARTED FOR " + resourceIds.size());
           long start = System.currentTimeMillis();
           LocalDate finalMonth = month;
           ArrayList<PayslipSetting> payslipSettings = new ArrayList<>();
           resourceContracts.keySet().forEach(resourceId -> {
               Long contractId = resourceContracts.get(resourceId);
               payload.setContractId(contractId);
               payload.setResourceId(resourceId);
               PayslipController.CalculatedPayslip calculatedPayslip = payslipService.calculate(payload, employeeMap.get(resourceId), false /*This must be false*/, false);
               PayslipSetting payslipSetting = payslipDraftService.prepareDraft(calculatedPayslip.template(), calculatedPayslip.calculatedElements(), contractId, resourceId, employeeMap.get(resourceId), finalMonth, true);
               payslipSettings.add(payslipSetting);
               i.getAndIncrement();
           });
           if(!payslipSettings.isEmpty()) {
               List<PayslipSetting> savedDrafts = payslipSettingRepository.saveAll(payslipSettings);
               hrDataRepository.bulkInsertDraftIds(month, savedDrafts.stream().map(PayslipSetting::getId).collect(Collectors.toList()));
           }
           long end = System.currentTimeMillis();
           System.out.println(resourceIds.size() + " OF RESOURCES UPDATED IN " + (end - start) + "ms");
       } catch (Exception e) {
           e.printStackTrace();
       } finally {
           RequestContext.clear();
       }


    }
}
