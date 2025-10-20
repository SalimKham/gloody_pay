package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.User;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

@Transactional
@Service
@RequiredArgsConstructor
public class MonthlyPayInitializer {
    private final HRDataRepository hrDataRepository;
    private final PayslipCloseService closeService;
    private final EmployeeRepository employeeRepository;

    public void initializeMonthlyPay(Long companyId, LocalDate month, LocalDate activeMonth) {
         companyCurrentVariables(companyId, month, activeMonth);
        Set<Long> resourceIds = employeeRepository.findAllIdsByCompanyId(companyId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(User.builder().username("").CompanyId(companyId).id(-1L).build(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        closeService.closePayslips(resourceIds, month);
    }

    private void companyCurrentVariables(Long companyId, LocalDate month, LocalDate activeMonth) {
        hrDataRepository.copyVariables(month, activeMonth, companyId);
    }
}
