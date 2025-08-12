package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.repositories.CompanyRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DasIndividualService {
    private final EmployeeRepository employeeRepository;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final CompanyRepository companyRepository;

    public byte[] generate(int year, Long resourceId) throws IOException {
        Company company = companyRepository.findById(SecurityUtils.getCurrentCompanyId()).orElse(null);
        Optional<Employee> data = employeeRepository.findById(resourceId);
        if (data.isEmpty() || company == null) {
            return null;
        }
        IndividualDasDTO individualDasDTO = new IndividualDasDTO();
        Employee employee = data.get();
        individualDasDTO.setFullName( Objects.requireNonNullElse(employee.getFullName(), "").toUpperCase());
        individualDasDTO.setYear(year);
        individualDasDTO.setBirthDate(employee.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        individualDasDTO.setRange(getRange(employee));
        individualDasDTO.setAffiliate(employee.getAffiliate());
        individualDasDTO.setService(employee.getOffice());
        individualDasDTO.setCompanyAddress(company.getAddress().toString());
        individualDasDTO.setDeclarationType("N");
        individualDasDTO.setMemberShip(company.getMembershipNumber());
        individualDasDTO.setService(company.getActivitySector());
        individualDasDTO.setSocialSecurity(employee.getSocialSecurity());
        double GROSS1 =  Objects.requireNonNullElse(payslipElementSettingRepository
                .findEmployeeGrossSalary(LocalDate.of(year, 1, 1), LocalDate.of(year, 3, 1), resourceId ), 0d);
        double GROSS2 =  Objects.requireNonNullElse(payslipElementSettingRepository
                .findEmployeeGrossSalary(LocalDate.of(year, 4, 1), LocalDate.of(year, 6, 1), resourceId ), 0d);
        double GROSS3 =  Objects.requireNonNullElse(payslipElementSettingRepository
                .findEmployeeGrossSalary(LocalDate.of(year, 7, 1), LocalDate.of(year, 9, 1), resourceId ), 0d);
        double GROSS4 =  Objects.requireNonNullElse(payslipElementSettingRepository
                .findEmployeeGrossSalary(LocalDate.of(year, 10, 1), LocalDate.of(year, 12, 1), resourceId ), 0d);
        double GROSS_YEAR = GROSS1 + GROSS2 + GROSS3 + GROSS4;
        Double WD1 = Objects.requireNonNullElse( payslipElementSettingRepository
                .findEmployeeWorkingDays(90d, LocalDate.of(year, 1, 1), LocalDate.of(year, 3, 1), resourceId ), 90d);
        Double WD2 = Objects.requireNonNullElse( payslipElementSettingRepository
                .findEmployeeWorkingDays(90d, LocalDate.of(year, 4, 1), LocalDate.of(year, 6, 1), resourceId ), 90d);
        Double WD3 = Objects.requireNonNullElse( payslipElementSettingRepository
                .findEmployeeWorkingDays(90d, LocalDate.of(year, 7, 1), LocalDate.of(year, 9, 1), resourceId ), 90d);
        Double  WD4 = Objects.requireNonNullElse( payslipElementSettingRepository
                .findEmployeeWorkingDays(90d, LocalDate.of(year, 10, 1), LocalDate.of(year, 12, 1), resourceId ), 90d);
      individualDasDTO.setGross1(BigDecimal.valueOf(GROSS1).setScale(2, RoundingMode.HALF_UP));
      individualDasDTO.setGross2(BigDecimal.valueOf(GROSS2).setScale(2, RoundingMode.HALF_UP));
      individualDasDTO.setGross3(BigDecimal.valueOf(GROSS3).setScale(2, RoundingMode.HALF_UP));
      individualDasDTO.setGross4(BigDecimal.valueOf(GROSS4).setScale(2, RoundingMode.HALF_UP));
      individualDasDTO.setGrossYear(BigDecimal.valueOf(GROSS_YEAR).setScale(2, RoundingMode.HALF_UP));
      individualDasDTO.setWd1(StringUtils.leftPad(String.valueOf(WD1.intValue()), 2, "0"));
      individualDasDTO.setWd2(StringUtils.leftPad(String.valueOf(WD2.intValue()), 2, "0"));
      individualDasDTO.setWd3(StringUtils.leftPad(String.valueOf(WD3.intValue()), 2, "0"));
      individualDasDTO.setWd4(StringUtils.leftPad(String.valueOf(WD4.intValue()), 2, "0"));

        return PayrollRecordsUtils.generateDocFromTemplate("das_individual", individualDasDTO);
    }


    private String getRange(Employee employee) {
        String hireDate = employee.getHireDate() != null ? employee.getHireDate().format(PayrollRecordsUtils.dateFormatter) : "__/__/____";
        String exitDate = employee.getExitDate() != null ? employee.getExitDate().format(PayrollRecordsUtils.dateFormatter) : "__/__/____";
        return String.format("%s    %s", hireDate, exitDate);
    }
}
