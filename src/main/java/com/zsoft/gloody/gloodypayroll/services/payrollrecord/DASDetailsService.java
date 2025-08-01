package com.zsoft.gloody.gloodypayroll.services.payrollrecord;

import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.repositories.CompanyRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DASDetailsService {
    private final EmployeeRepository employeeRepository;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final CompanyRepository companyRepository;

    private Map<Long, Double> firstQuarterContributions = new HashMap<>();
    private Map<Long, Double> secondQuarterContributions = new HashMap<>();
    private Map<Long, Double> thirdQuarterContributions = new HashMap<>();
    private Map<Long, Double> forthQuarterContributions = new HashMap<>();
    private Map<Long, Double> firstQuarterWorkingDays = new HashMap<>();
    private Map<Long, Double> secondQuarterWorkingDays = new HashMap<>();
    private Map<Long, Double> thirdQuarterWorkingDays = new HashMap<>();
    private Map<Long, Double> forthQuarterWorkingDays = new HashMap<>();

    public StringBuffer generate(int year) {
        int PAGE_SIZE = 500;
        String employerNumber = companyRepository.findById(SecurityUtils.getCurrentCompanyId()).map(Company::getMembershipNumber).orElse("");
        Page<Employee> data = employeeRepository.findYearlyEmployees(year, Pageable.ofSize(PAGE_SIZE));
        if (data.isEmpty()) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        do {
            initQuarterContribution(year);
            initQuarterWorkingDays(year);
            data.forEach(employee -> {
                stringBuffer.append(transform(employee, year, employerNumber ));
                stringBuffer.append("\n");
            });


            if (data.hasNext()) {
                data =  employeeRepository.findYearlyEmployees(year, data.nextPageable());
            } else {
                break;
            }
        }while (true);
        return stringBuffer;
    }

    private void initQuarterContribution(int year) {
        firstQuarterContributions = payslipElementSettingRepository
                .findQuarterlyContribution(LocalDate.of(year, 1, 1), LocalDate.of(year, 3, 1) )
                .stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
        secondQuarterContributions = payslipElementSettingRepository
                .findQuarterlyContribution(LocalDate.of(year, 4, 1), LocalDate.of(year, 6, 1) )
                .stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
        thirdQuarterContributions = payslipElementSettingRepository
                .findQuarterlyContribution(LocalDate.of(year, 7, 1), LocalDate.of(year, 9, 1) )
                .stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
        forthQuarterContributions = payslipElementSettingRepository
                .findQuarterlyContribution(LocalDate.of(year, 10, 1), LocalDate.of(year, 12, 1) )
                .stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
    }

    // todo check working days
    private void initQuarterWorkingDays(int year) {
        firstQuarterWorkingDays = payslipElementSettingRepository
                .findQuarterlyWorkingDays(90d, LocalDate.of(year, 1, 1), LocalDate.of(year, 3, 1) )
                .stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
        secondQuarterWorkingDays = payslipElementSettingRepository
                .findQuarterlyWorkingDays(90d, LocalDate.of(year, 4, 1), LocalDate.of(year, 6, 1) )
                .stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
        thirdQuarterWorkingDays = payslipElementSettingRepository
                .findQuarterlyWorkingDays(90d, LocalDate.of(year, 7, 1), LocalDate.of(year, 9, 1) )
                .stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
        forthQuarterWorkingDays = payslipElementSettingRepository
                .findQuarterlyWorkingDays(90d, LocalDate.of(year, 10, 1), LocalDate.of(year, 12, 1) )
                .stream().collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
    }

    private List<DasQuarter> getEmployeeQuarters(Long employeeId) {
        List<DasQuarter> quarters = new ArrayList<>(4);
        quarters.add(new DasQuarter(firstQuarterWorkingDays.getOrDefault(employeeId, 90d ), firstQuarterContributions.getOrDefault(employeeId, 0d)));
        quarters.add(new DasQuarter(secondQuarterWorkingDays.getOrDefault(employeeId, 90d ), secondQuarterContributions.getOrDefault(employeeId, 0d)));
        quarters.add(new DasQuarter(thirdQuarterWorkingDays.getOrDefault(employeeId, 90d ), thirdQuarterContributions.getOrDefault(employeeId, 0d)));
        quarters.add(new DasQuarter(forthQuarterWorkingDays.getOrDefault(employeeId, 90d ), forthQuarterContributions.getOrDefault(employeeId, 0d)));
        return quarters;
    }

    // todo check the date format
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private  String transform(Employee employee, int year, String employerNumber) {
        List<DasQuarter> quarters = getEmployeeQuarters(employee.getId());
        StringBuilder sb = new StringBuilder(200); // Approximate total length

        // Employer No. (10 chars)
        appendPadded(sb, employerNumber, 10);

        // Year (4 chars)
        appendPadded(sb, String.valueOf(year), 4);

        // Sequential No. (6 chars)
        appendPadded(sb, "999999", 6); // todo generate this

        // SS No. (12 chars)
        appendPadded(sb, employee.getSocialSecurity(), 12);

        // Last name (25 chars)
        appendPadded(sb, employee.getLastName(), 25);

        // First names (25 chars)
        appendPadded(sb, employee.getFirstName(), 25);

        // Date of birth (8 chars)
        appendDate(sb, employee.getBirthDate());

        // Space (1 char)
        sb.append(" ");

        // Process quarterly data
        processQuarter(sb, quarters.get(0));
        processQuarter(sb,  quarters.get(1));
        processQuarter(sb,  quarters.get(2));
        processQuarter(sb,  quarters.get(3));

        // Annual contributory salary (12 chars, 2 decimal places)
        appendPadded(sb, formatSalary(quarters.stream().mapToDouble(DasQuarter::contributorySalary).sum()), 12);

        //Seniority date (8 chars)
        appendDate(sb, employee.getSeniority_date());
        if (employee.getExitDate() == null) {
            appendPadded(sb, "", 8);
        } else {
            appendDate(sb, employee.getExitDate());
        }

        return sb.toString();
    }

    private  void appendPadded(StringBuilder sb, String value, int length) {
        if (value == null) value = "";
        if (value.length() > length) {
            sb.append(value.substring(0, length));
        } else {
            sb.append(String.format("%-" + length + "s", value));
        }
    }

    private  void appendDate(StringBuilder sb, LocalDate date) {
        if (date != null) {
            sb.append(date.format(DATE_FORMATTER));
        } else {
            sb.append("        "); // 8 spaces
        }
    }

    private  void processQuarter(StringBuilder sb, DasQuarter quarter) {
        // Days worked (2 chars)
        appendPadded(sb, String.valueOf(quarter.daysWorked()), 2);

        sb.append("J");

        // Contributory salary (11 chars, 2 decimal places)
        appendPadded(sb, formatSalary(quarter.contributorySalary()), 11);
    }

    private  String formatSalary(double amount) {
        return String.format("%.2f", amount).replace(",", "").replace(".", "");
    }
}
