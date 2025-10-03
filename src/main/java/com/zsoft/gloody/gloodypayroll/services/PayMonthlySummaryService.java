package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionType;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.MonthlySummaryDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.MonthlySummaryPayloadDTO;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingAccountDTO;
import com.zsoft.gloody.gloodypayroll.rest.mappers.accounting.AccountingSettingAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Transactional
public class PayMonthlySummaryService {
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final AccountingSettingAccountMapper accountMapper;

    public List<MonthlySummaryDto> getMonthlySummary(MonthlySummaryPayloadDTO payload, Pageable pageable) {
          if (payload.getEmployees() == null || payload.getEmployees().isEmpty()) {
              return null;
          }
        Sort sort =pageable.getSort();
         return payslipElementSettingRepository
                 .sumElementsByEmployeeIdAndMonth(payload.getEmployees(), payload.getMonth().withDayOfMonth(1),  Pageable.unpaged(sort))
                 .stream()
                 .map(tuple -> {
                     Section section = tuple.get(0, Section.class);
                     if (section == null) {
                         return null;
                     }
                     if (shouldFilterByLabels(section, payload.getLabels())) {
                         return null;
                     }
                     AccountingSettingAccountDTO accountDTO = accountMapper.toDto(section.getAccounting());
                     String  code  = String.format("R%s",section.getIdentifier());
                     String  label  = section.getLabel();
                     return MonthlySummaryDto.builder()
                             .code(code)
                             .label(label)
                             .account(accountDTO)
                             .base(tuple.get(1, Double.class))
                             .rate(tuple.get(2, Double.class))
                             .gain(Objects.equals(section.getType(), SectionType.GAIN) ? tuple.get(3, Double.class) : null)
                             .deduction(Objects.equals(section.getType(), SectionType.DEDUCTION) ? tuple.get(3, Double.class) : null)
                             .numberOfEmployees(Objects.requireNonNullElse(tuple.get(4, Long.class), 0).intValue())
                             .build();
                 }).filter(Objects::nonNull).toList();

    }

    private boolean shouldFilterByLabels(Section section, Set<String> payloadLabels) {
        if (payloadLabels == null || payloadLabels.isEmpty()) {
            return false;
        }

        List<String> labelKeys = section.getLabelKeys();
        return labelKeys == null || labelKeys.isEmpty() ||
                labelKeys.stream().noneMatch(payloadLabels::contains);
    }
}
