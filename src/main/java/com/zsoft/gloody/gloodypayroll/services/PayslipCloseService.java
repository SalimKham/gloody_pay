package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.RequestContext;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipClose;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipCloseRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipCalculatePayload;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapper;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PayslipCloseService {
    private final PayslipSettingRepository payslipSettingRepository;
    private final HRDataRepository hrDataRepository;
    private final EmployeeMapper employeeMapper;
    // TODO CHECK IF THIS STILL NEEDED
    private final EmployeeRepository employeeRepository;
    private final PayslipCloseRepository payslipCloseRepository;
    private final PayslipService payslipService;

    private Map<Long, List<Long>> loadContracts(Set<Long> resourceIds, LocalDate month) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return hrDataRepository.getContractIdsByMonthAndResourceIdIn(month.withDayOfMonth(1), resourceIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(0, Long.class),
                        Collectors.mapping(
                                tuple -> tuple.get(1, Long.class),
                                Collectors.toCollection(ArrayList::new)
                        )
                ));
    }

    public Set<Long> closePayslips(Set<Long> resourceIds, LocalDate month) {
        log.info("CLOSING PAYSLIPS FOR EMPLOYEES {} | MONTH {}", resourceIds, month);
        Set<Long> closedPayslips = new HashSet<>();
        Set<Long> failed = new HashSet<>();
        month = month.withDayOfMonth(1);
        if (resourceIds.isEmpty()) {
            return Collections.emptySet();
        }
        Map<Long, List<Long>> contracts = loadContracts(resourceIds, month);
        if (contracts.isEmpty()) {
            return Collections.emptySet();
        }
        if (contracts.size() > 20) {
            RequestContext.initTemplates();
        }
        RequestContext.initMonthlyValues(contracts.keySet());
        payslipService.initFunctionElements(contracts.keySet());
        Set<Long> closedContractIds = new HashSet<>();
        Map<Long, Employee> employeeMap = employeeRepository.findAllById(contracts.keySet()).stream().collect(Collectors.toMap(Employee::getId, em -> em));
        PayslipCalculatePayload payload = new PayslipCalculatePayload();
        for (Long resourceId : resourceIds) {
            try {
                if (contracts.containsKey(resourceId)) {
                    Set<Long> resourceContracts = new HashSet<>(contracts.get(resourceId));
                    for (Long contractId : resourceContracts) {
                        payload.setValues(resourceId, contractId, month);
                        PayslipController.CalculatedPayslip calculatedPayslip = payslipService.calculate(payload, employeeMap.getOrDefault(resourceId, null), false, false);
                        if (calculatedPayslip != null) {
                            LocalDate finalMonth = month;
                            payslipCloseRepository.deleteByContractIdAndMonth(contractId, month);
                            payslipCloseRepository.saveAll(calculatedPayslip.calculatedElements()
                                    .stream()
                                    .map(el -> PayslipClose.builder().contractId(contractId)
                                            .month(finalMonth)
                                            .sectionCode(el.getCode())
                                            .baseValue(Objects.nonNull(el.getBase()) ? el.getBase().doubleValue() : 0d)
                                            .rateValue(Objects.nonNull(el.getRate()) ? el.getRate().doubleValue() : 0d)
                                            .value(Objects.nonNull(el.getValue()) ? el.getValue() : 0d)
                                            .build()

                                    ).toList()
                            );
                            // save new Payslip
                            PayslipSetting closedPayslip = payslipSettingRepository.saveAndFlush(PayslipSetting.
                                    builder()
                                    .name(String.format("%s-%s-%s", resourceId, contractId, month))
                                    .closed(true)
                                    .company(SecurityUtils.getCurrentCompanyId())
                                    .template(false)
                                    .showIntermediate(true)
                                    .byDefault(false)
                                    .draft(false)
                                    .rate(calculatedPayslip.template().getRate())
                                    .contract(contractId)
                                    .elements(null)
                                    .employee(resourceId)
                                    .approved(false)
                                    .paymentDate(month)
                                    .payslipDetail(employeeMapper.employeeToPayslipDetails(employeeMap.getOrDefault(resourceId, null), contractId, month))
                                    .build()
                            );
                            payslipSettingRepository.insertClosedPayslip(calculatedPayslip.template().getId(), closedPayslip.getId(), month, contractId);
                            payslipSettingRepository.deleteByContractAndPaymentDateAndIdNot(contractId, finalMonth, closedPayslip.getId());
                            closedPayslips.add(closedPayslip.getId());
                            closedContractIds.add(contractId);
                        }
                    }
                }
            } catch (Exception e) {
                failed.add(resourceId);
            }
        }
        RequestContext.clear();
        if (!closedPayslips.isEmpty()) {
            hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("TEMPLATE", closedContractIds, month);
            hrDataRepository.bulkInsertClosedTemplateIds(month, closedPayslips);
        }
        return failed;
    }


}
