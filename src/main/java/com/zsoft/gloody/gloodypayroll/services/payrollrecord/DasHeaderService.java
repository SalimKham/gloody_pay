package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import com.zsoft.gloody.gloodypayroll.models.repositories.CompanyRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class DasHeaderService {

    private final CompanyRepository companyRepository;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final PayslipSettingRepository payslipSettingRepository;

    public String generate(int year, String affiliate, String affiliateAddress) {
        Company company =  companyRepository.findById(SecurityUtils.getCurrentCompanyId()).orElse(null);
        if (company == null) {
            return null;
        }
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 1);
        long totalEmployee = payslipSettingRepository.countAllClosed(startDate, endDate, affiliate);
        Double firstQuarter = Objects.requireNonNullElse(payslipElementSettingRepository.sumQuarterContribution(LocalDate.of(year, 1, 1), LocalDate.of(year, 3, 1), affiliate), 0d);
        Double secondQuarter =  Objects.requireNonNullElse(payslipElementSettingRepository.sumQuarterContribution(LocalDate.of(year, 4, 1), LocalDate.of(year, 6, 1), affiliate), 0d);
        Double thirdQuarter =  Objects.requireNonNullElse(payslipElementSettingRepository.sumQuarterContribution(LocalDate.of(year, 7, 1), LocalDate.of(year, 9, 1), affiliate), 0d);
        Double forthQuarter =  Objects.requireNonNullElse(payslipElementSettingRepository.sumQuarterContribution(LocalDate.of(year, 10, 1), LocalDate.of(year, 12, 1), affiliate), 0d);
        return buildDasCode(company.getMembershipNumber(), year, affiliate,  affiliateAddress, firstQuarter, secondQuarter, thirdQuarter, forthQuarter, totalEmployee);
    }

    private String buildDasCode(
        String employerNumber,
        int year,
        String activitySector,
        String address,
        Double firstQuarter,
        Double secondQuarter,
        Double thirdQuarter,
        Double forthQuarter,
        long totalEmployees
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append(padRight(employerNumber, 10));
        sb.append("N").append(year);

        sb.append("11900");
        sb.append(padRight(activitySector, 30));
        sb.append(padRight(address, 80));
        sb.append(formatSalary(firstQuarter));
        sb.append(formatSalary(secondQuarter));
        sb.append(formatSalary(thirdQuarter));
        sb.append(formatSalary(forthQuarter));
        sb.append(formatSalary(firstQuarter + secondQuarter + thirdQuarter + forthQuarter));
        sb.append(padLeft(String.valueOf(totalEmployees), 6, '0'));

        return sb.toString();
    }

    private static String padRight(String value, int length) {
        if (value == null) value = "";
        return String.format("%-" + length + "." + length + "s", value);
    }

    private static String padLeft(String value, int length, char padChar) {
        if (value == null) value = "";
        if (value.length() >= length) return value;
        return String.format("%" + length + "s", value).replace(' ', padChar);
    }

    private static String formatSalary(Double amount) {
        if (amount == null) amount = 0d;
        BigDecimal decimalAmmount = new BigDecimal(amount);
        BigDecimal scaled = decimalAmmount.setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        return padLeft(scaled.toBigInteger().toString(), 16, '0');
    }
}
