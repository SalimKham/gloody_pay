package com.zsoft.gloody.gloodypayroll.utils;

import com.zsoft.gloody.gloodypayroll.models.entities.Address;
import com.zsoft.gloody.gloodypayroll.models.entities.Company;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.services.EntityManagerService;
import com.zsoft.gloody.gloodypayroll.services.SectionDictionaryService;
import lombok.AllArgsConstructor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.*;

@AllArgsConstructor
@Component
public class TemplateHelper extends EntityManagerService {
    private final SectionDictionaryService dictionaryService;
    private final Map<String, ResourceBundleMessageSource> messageSources = new HashMap<>();

    private ResourceBundleMessageSource getMessageSource(String file) {
        if (!messageSources.containsKey(file)) {
            ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
            ms.setBasenames("i18n/" + file);
            ms.setDefaultEncoding("UTF-8");
            messageSources.put(file, ms);
        }
        return messageSources.get(file);
    }

    public Map<String, Object> getPayrollData(PayslipSettingDto dto, Locale locale) {
        Map<String, Object> data = new HashMap<>();
        data.put("payroll_start", dictionaryService.findValue(PAYMENT_START));
        data.put("payroll_end", dictionaryService.findValue(PAYMENT_END));

        data.put("show_employer_col", false);
        data.put("currency", getLocaleCurrency(locale));
        double hoursNbr = dictionaryService.findDoubleValue(HOURS_NBR);
        double baseSalary = dictionaryService.findDoubleValue(BASE_SALARY);
        data.put("emp_base_salary", baseSalary);
        data.put("monthly_duration", hoursNbr);
        data.put("hourly_rate", hoursNbr > 0 ? baseSalary / hoursNbr : 0);

        Long employeeId = Optional.ofNullable(dto.getEmployee()).map(EmployeeDto::getId).orElse(null);
        data.putAll(getEmployeeData(employeeId, locale));
        return data;
    }

    public Map<String, Object> getEmployeeData(Long employeeId, Locale locale) {
        Map<String, Object> data = new HashMap<>();
        if (employeeId == null) {
            return data;
        }
        Employee employee = getProxyEntity(Employee.class, employeeId);
        if (employee != null) {
            data.put("emp_full_name", formatFullName(employee));
            data.put("emp_address", getFormattedAddress(employee.getAddress(), locale));
            data.put("emp_social_security_number", employee.getSocialSecurityNumber());
            data.put("contract_start", employee.getEnteredDate());
            data.put("contract_end", null);
            data.put("emp_job_classification", employee.getJobClassification());
            data.put("emp_job_category", employee.getJobCategory());
            data.put("emp_job_title", employee.getJobTitle());
            data.put("emp_department", employee.getDepartment());

            Long companyId = Optional.ofNullable(employee.getCompany()).map(Company::getId).orElse(null);
            data.putAll(getCompanyData(companyId, locale));
        }
        return data;
    }

    public Map<String, Object> getCompanyData(Long companyId, Locale locale) {
        Map<String, Object> data = new HashMap<>();
        if (companyId == null) {
            return data;
        }
        Company company = getProxyEntity(Company.class, companyId);
        if (company != null) {
            data.put("cmp_name", company.getName());
            data.put("cmp_adr", getFormattedAddress(company.getAddress(), locale));
            data.put("cmp_registration_number", "1231669778820");
            data.put("cmp_tax_number", "V123321619940");
        }
        return data;
    }

    public String formatFullName(String firstName, String lastName) {
        if ((firstName == null || firstName.isEmpty()) && (lastName == null || lastName.isEmpty())) {
            return "";
        }
        if (firstName == null || firstName.isEmpty()) {
            return StringUtils.capitalize(lastName);
        }
        if (lastName == null || lastName.isEmpty()) {
            return StringUtils.capitalize(firstName);
        }
        return StringUtils.capitalize(firstName) + " " + lastName.toUpperCase();
    }

    public String formatFullName(Employee employee) {
        if (employee == null) {
            return "";
        }
        return formatFullName(employee.getFirstName(), employee.getLastName());
    }

    public String getFormattedAddress(Address address, Locale locale) {
        return getFormattedAddress(address, locale, true);
    }

    public String getFormattedAddress(Address address, Locale locale, boolean inline) {
        if (address == null) {
            return "";
        }
        List<String> addressInline = new ArrayList<>();
        if (address.getAddressLine1() != null && !address.getAddressLine1().isEmpty()) {
            addressInline.add(address.getAddressLine1().trim() + "\n");
        }
        if (address.getAddressLine2() != null && !address.getAddressLine2().isEmpty()) {
            addressInline.add(address.getAddressLine2().trim() + "\n");
        }
        if (address.getCity() != null && !address.getCity().isEmpty()) {
            addressInline.add(address.getCity().trim());
        }
        if (address.getState() != null && !address.getState().isEmpty()) {
            addressInline.add(address.getState().trim());
        }
        if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
            addressInline.add(address.getPostalCode().trim());
        }
        if (address.getCountryCode() != null && !address.getCountryCode().isEmpty()) {
            String country = getCountryName(address.getCountryCode(), locale);
            addressInline.add(country);
        }
        if (inline) {
            return String.join(" ", addressInline).replaceAll("\n", " ");
        }
        return String.join(" ", addressInline).replaceAll("\n", "<br />");
    }

    public String formatDate(LocalDateTime date, String format, Locale locale) {
        return date.format(DateTimeFormatter.ofPattern(format, locale));
    }

    public String formatDate(LocalDate date, String format, Locale locale) {
        return formatDate(date.atTime(LocalTime.now()), format, locale);
    }

    public String getCountryName(String code, Locale locale) {
        if (code == null) {
            return null;
        }
        if (locale == null) {
            locale = Locale.FRENCH;
        }
        return getMessageSource("countries").getMessage(code, null, code, locale);
    }

    public String getLocaleCurrency(Locale locale) {
        try {
            Currency currency = Currency.getInstance(locale);
            if (currency != null) {
                return currency.getCurrencyCode();
            }
        } catch (IllegalArgumentException ignored) {
        }
        return "EUR";
    }
}
