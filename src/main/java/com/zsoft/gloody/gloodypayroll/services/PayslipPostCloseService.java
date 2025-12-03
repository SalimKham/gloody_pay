package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PayslipPostCloseService {
    private final HRDataRepository hrDataRepository;
    private final PayslipSettingRepository payslipSettingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postClose(Set<Long> contractIds, LocalDate month) {
        log.info("SAVING NEW TEMPLATE IDS IN THE MONTHLY VALUES");
        payslipSettingRepository.insertClosedPayslip(month, contractIds);
        hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("TEMPLATE", contractIds, month);
        hrDataRepository.bulkInsertClosedTemplateIds(month, contractIds);
    }

}
