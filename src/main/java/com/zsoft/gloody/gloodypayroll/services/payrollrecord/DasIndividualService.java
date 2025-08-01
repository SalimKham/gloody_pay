package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.repositories.CompanyRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        IndividualDasDTO individualDasDTO = new IndividualDasDTO();
        Employee employee = data.get();
        individualDasDTO.setFullName( Objects.requireNonNullElse(employee.getFullName(), "").toUpperCase());
        individualDasDTO.setYear(year);
        individualDasDTO.setBirthDate(employee.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        individualDasDTO.setRange(getRange(employee, formatter));
        individualDasDTO.setAffiliate(employee.getAffiliate());
        individualDasDTO.setService(employee.getOffice());
        individualDasDTO.setCompanyAddress(company.getAddress().toString());
        individualDasDTO.setDeclarationType("N");
        individualDasDTO.setMemberShip(company.getMembershipNumber());
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
        double WD1 = Objects.requireNonNullElse( payslipElementSettingRepository
                .findEmployeeWorkingDays(90d, LocalDate.of(year, 1, 1), LocalDate.of(year, 3, 1), resourceId ), 90d);
        double WD2 = Objects.requireNonNullElse( payslipElementSettingRepository
                .findEmployeeWorkingDays(90d, LocalDate.of(year, 4, 1), LocalDate.of(year, 6, 1), resourceId ), 90d);
        double WD3 = Objects.requireNonNullElse( payslipElementSettingRepository
                .findEmployeeWorkingDays(90d, LocalDate.of(year, 7, 1), LocalDate.of(year, 9, 1), resourceId ), 90d);
        double  WD4 = Objects.requireNonNullElse( payslipElementSettingRepository
                .findEmployeeWorkingDays(90d, LocalDate.of(year, 10, 1), LocalDate.of(year, 12, 1), resourceId ), 90d);
      individualDasDTO.setGross1(GROSS1);
      individualDasDTO.setGross2(GROSS2);
      individualDasDTO.setGross3(GROSS3);
      individualDasDTO.setGross4(GROSS4);
      individualDasDTO.setGrossYear(GROSS_YEAR);
      individualDasDTO.setWd1(WD1);
      individualDasDTO.setWd2(WD2);
      individualDasDTO.setWd3(WD3);
      individualDasDTO.setWd4(WD4);

        return PayrollRecordsUtils.generateDocFromTemplate("das_individual", individualDasDTO);
    }


    private String getRange(Employee employee, DateTimeFormatter formatter) {
        String hireDate = employee.getHireDate() != null ? employee.getHireDate().format(formatter) : "__/__/____";
        String exitDate = employee.getExitDate() != null ? employee.getExitDate().format(formatter) : "__/__/____";
        return String.format("%s    %s", hireDate, exitDate);
    }
}
