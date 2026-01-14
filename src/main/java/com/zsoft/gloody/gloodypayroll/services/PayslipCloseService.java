package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.CacheControl;
import com.zsoft.gloody.gloodypayroll.configs.Constants;
import com.zsoft.gloody.gloodypayroll.configs.RequestContext;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipClose;
import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.SummaryDataType;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipCloseRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipCalculatePayload;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipCloseResultDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapper;
import com.zsoft.gloody.gloodypayroll.utils.CommonUtils;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import com.zsoft.gloody.gloodypayroll.utils.TransactionSyncUtil;
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
    private final PayslipPostCloseService payslipPostCloseService;

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
        CacheControl.enable();
        payslipCloseRepository.clearAllData();
        Set<Long> failed = new HashSet<>();
        month = month.withDayOfMonth(1);
        if (resourceIds.isEmpty()) {
            return Collections.emptySet();
        }
        Map<Long, List<Long>> contracts = loadContracts(resourceIds, month);
        if (contracts.isEmpty()) {
            return Collections.emptySet();
        }
        RequestContext.initMonthlyValues(month, contracts.keySet());
        Set<Long> loadContractIds = contracts.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        payslipService.initFunctionElements(contracts.keySet());
        Set<Long> closedContractIds = new HashSet<>();
        Map<Long, Employee> employeeMap = employeeRepository.findAllById(contracts.keySet()).stream().collect(Collectors.toMap(Employee::getId, em -> em));
        payslipSettingRepository.deleteByContractInAndPaymentDate(loadContractIds, month.withDayOfMonth(1));
        PayslipCalculatePayload payload = new PayslipCalculatePayload();
        List<HRMonthlyData> baseSalaries = new ArrayList<>();
        List<PayslipClose> closedElements = new ArrayList<>();
        List<PayslipSetting> closedPayslipSettings = new ArrayList<>();
        long startClosingTime = System.currentTimeMillis();
        int closedCount = 0;
        for (Long resourceId : resourceIds) {
            long start = System.currentTimeMillis();
            try {
                if (contracts.containsKey(resourceId)) {
                    Set<Long> resourceContracts = new HashSet<>(contracts.get(resourceId));
                    for (Long contractId : resourceContracts) {
                        payload.setValues(resourceId, contractId, month);
                        PayslipController.CalculatedPayslip calculatedPayslip = payslipService.calculate(payload, employeeMap.getOrDefault(resourceId, null), false);
                        if (calculatedPayslip != null) {
                            LocalDate finalMonth = month;
                            closedElements.addAll(calculatedPayslip.calculatedElements()
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
                            assert employeeMap.containsKey(resourceId);
                            Employee employee = employeeMap.get(resourceId);

                            closedPayslipSettings.add(PayslipSetting.
                                    builder()
                                    .name(String.format("%s-%s-%s", resourceId, contractId, month))
                                    .closed(true)
                                    .company(SecurityUtils.getCurrentCompanyId())
                                    .template(false)
                                    .showIntermediate(true)
                                    .byDefault(false)
                                    .draft(false)
                                    .showZero(calculatedPayslip.template().getShowZero())
                                    .rate(calculatedPayslip.template().getRate())
                                    .contract(contractId)
                                    .elements(null)
                                    .employee(resourceId)
                                    .approved(false)
                                    .paymentDate(month)
                                    .parentTemplateId(calculatedPayslip.template().getId())
                                    .payslipDetail(employeeMapper.employeeToPayslipDetails(employeeMap.getOrDefault(resourceId, null), contractId, month))
                                    .build()
                            );
                            baseSalaries.add(HRMonthlyData.builder().variable(Constants.BASE_SALARY).value(Objects.requireNonNullElse(employee.getBaseSalary(), 0d)).month(month).contractId(contractId).resourceId(resourceId).type(SummaryDataType.SALARY).build());
                            closedContractIds.add(contractId);
                        }
                    }
                }
                long end = System.currentTimeMillis();
                log.info("CLOSING PAYLIPS {}/{} IN {} ms",++ closedCount, resourceIds.size(), (end - start));
            } catch (Exception e) {
                log.error("CLOSING PAYLIPS ERROR employee id {}", resourceId, e);
                failed.add(resourceId);
            }
        }
        CacheControl.disable();
        RequestContext.clear();
        log.info("BULK SAVING RESULT");
        if (!closedContractIds.isEmpty()) {
            log.info("SAVING CALCULATED ELEMENTS");
            payslipCloseRepository.saveAll(closedElements);
            log.info("SAVING NEW PAYSLIPS");
          payslipSettingRepository.saveAll(closedPayslipSettings);
            LocalDate postCloseMonth = month;
            TransactionSyncUtil.afterCommit(() -> {
                CommonUtils.splitSet(closedContractIds, 300).parallelStream().forEach(batch -> payslipPostCloseService.postClose(batch, postCloseMonth));
                log.info("CLOSING PAYSLIPS FOR EMPLOYEES: {} | MONTH: {} DONE", resourceIds, postCloseMonth);
                log.info("ELAPSED TIME: {} ", (System.currentTimeMillis() - startClosingTime));
                log.info("FAILED: {} ", failed.size());
            });

        }
        if (!baseSalaries.isEmpty()) {
            hrDataRepository.deleteAllByVariableAndContractIdInAndMonth(Constants.BASE_SALARY, closedContractIds, month);
            hrDataRepository.saveAll(baseSalaries);
        }

        return failed;
    }

    public PayslipCloseResultDto getPayslipCloseResult(LocalDate month) {
        Long closed = payslipSettingRepository.countClosedByPaymentDate(month.withDayOfMonth(1), SecurityUtils.getCurrentCompanyId());
        Long total = hrDataRepository.countMonthlyPayslips(month.withDayOfMonth(1), SecurityUtils.getCurrentCompanyId());
        return new PayslipCloseResultDto(closed.intValue(), total.intValue());
    }

    public void reOpenPayslip(LocalDate month, Long resourceId, Long contractId, Long templateId) {
        hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("TEMPLATE", Collections.singleton(contractId), month.withDayOfMonth(1));
        payslipSettingRepository.deleteByContractInAndPaymentDate(Collections.singleton(contractId), month.withDayOfMonth(1));
        if (Objects.nonNull(templateId)) {
            hrDataRepository.save(HRMonthlyData.builder()
                    .type(SummaryDataType.TEMPLATE)
                    .variable("TEMPLATE")
                    .resourceId(resourceId)
                    .contractId(contractId)
                    .month(month.withDayOfMonth(1))
                    .value(templateId)
                    .build());
        }
    }
}
