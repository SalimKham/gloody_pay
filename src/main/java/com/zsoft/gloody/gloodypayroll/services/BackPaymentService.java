package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.BackPayment;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.repositories.BackPaymentsRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.BackPaymentDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipCalculatePayload;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.BackPaymentMapper;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BackPaymentService extends CompanySupportService {

    private final BackPaymentsRepository repository;
    private final BackPaymentMapper mapper;
    private final PayslipService payslipService;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final SectionRepository sectionRepository;

    public void saveBackPayment(BackPaymentDto dto) {
        dto.setMonth(dto.getMonth().withDayOfMonth(1));
        Map<String, Double> backPaymentsMap = getBackPaymentsAsMap(dto);
        BackPayment backPayment = mapper.toEntity(dto);
        backPayment.setSectionCode(null);
        BackPayment savedBackPayment = repository.saveAndFlush(backPayment);
        if(backPaymentsMap.isEmpty()) {
            return;
        }
        Map<String, Section> sectionMap = sectionRepository.getSectionsTitles(backPaymentsMap.keySet(), SecurityUtils.getCurrentCompanyId()).stream().collect(Collectors.toMap(t -> t.get(0, String.class), t -> t.get(1, Section.class) ));
        backPaymentsMap.forEach((sectionCode, value) -> {
            if (value != 0) {
                repository.save(BackPayment
                        .builder()
                                .code("RG-" + sectionMap.get(sectionCode).getIdentifier())
                                .contractId(dto.getContractId())
                                .resourceId(dto.getResourceId())
                                .sectionCode(sectionCode)
                                .title(sectionMap.get(sectionCode).getLabel())
                                .startMonth(dto.getStartMonth())
                                .endMonth(dto.getEndMonth())
                                .month(dto.getMonth())
                                .source(savedBackPayment)
                                .value(value)
                        .build());
            }
        });
    }

    public void deleteBackPayment(Long backPaymentId) {
        repository.deleteById(backPaymentId);
    }

    public List<BackPaymentDto> getBackPayments(LocalDate month) {
        return repository.findAllByMonthAndSectionCodeIsNullAndSourceIsNull(month.withDayOfMonth(1)).stream().map(mapper::toDto).toList();
    }

    private Map<String, Double> getBackPaymentsAsMap(BackPaymentDto backPaymentDto) {
        Map<String, Double> backPaymentsMap = new HashMap<>();
        LocalDate startMoth = backPaymentDto.getStartMonth().withDayOfMonth(1);
        LocalDate endMoth = backPaymentDto.getEndMonth().withDayOfMonth(1);
        while (!startMoth.isAfter(endMoth)) {
            calculateDiffs(backPaymentsMap, startMoth, backPaymentDto);
            startMoth = startMoth.plusMonths(1);
        }
        return backPaymentsMap;
    }

    private void calculateDiffs(Map<String, Double> backPayments, LocalDate month, BackPaymentDto backPaymentDto) {

        Map<String, Double> oldValues = extractSavedValues(month, backPaymentDto.getContractId());
        Map<String, Double> newValues = calculateValues(PayslipCalculatePayload.builder()
                .contractId(backPaymentDto.getContractId())
                .month(month)
                .adjustBackPayments(true)
                .resourceId(backPaymentDto.getResourceId())
                .reCalculate(true)
                .backPayment(backPaymentDto)
                .build());
        oldValues.forEach((sectionCode, value) -> {
            double diff = newValues.getOrDefault(sectionCode, 0d) - Objects.requireNonNullElse(value, 0d);
            backPayments.put(sectionCode, backPayments.getOrDefault(sectionCode, 0d) + diff);
        });
    }

    private Map<String, Double> extractSavedValues(LocalDate month, Long contractId) {
        return payslipElementSettingRepository.getClosedElementsByContractIdAndMonth(contractId, month).stream().collect(Collectors.toMap(tuple -> tuple.get(0, String.class), tuple -> tuple.get(1, Double.class)));
    }

    private Map<String, Double> calculateValues(PayslipCalculatePayload calculatePayload) {
        PayslipController.CalculatedPayslip calculatedPayslip = payslipService.calculate(calculatePayload, null, false, true);
        if (calculatedPayslip == null || calculatedPayslip.calculatedElements() == null) {
            return Collections.emptyMap();
        }
        return calculatedPayslip.calculatedElements().stream().collect(Collectors.toMap(PayslipElementDto::getCode, payslipElementDto -> Objects.requireNonNullElse(payslipElementDto.getValueWithBackPayments(), payslipElementDto.getValue())));

    }
}
