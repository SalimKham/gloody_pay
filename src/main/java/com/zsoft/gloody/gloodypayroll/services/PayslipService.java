package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionCategory;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.EmployeeDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipElementDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingElementDto;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import com.zsoft.gloody.gloodypayroll.utils.TemplateHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import javax.script.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.*;

@Service
@AllArgsConstructor
public class PayslipService {
    private final SectionRepository sectionRepository;
    private final SectionDictionaryService dictionaryService;
    private final FormulaEngineService engineService;
    private final SectionSettingRepository sectionSettingRepository;
    private final TemplateHelper templateHelper;
    private final SpringTemplateEngine templateEngine;
    private final EmployeeService employeeService;
    private ScriptEngine engine;

    public String print(PayslipSettingDto dto) {
        EmployeeDto employeeDto = dto.getEmployee();
        if (employeeDto == null) {
            throw new ApiRuntimeException("error.employee.required");
        }
        /*Long employeeId = employeeDto.getId();
        if (employeeId == null || employeeService.notExistsById(employeeId)) {
            employeeService.create(employeeDto);
        } else {
            employeeService.update(employeeDto);
        }*/
        LinkedList<PayslipElementDto> elements = calculate(dto);
        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        context.setVariable("locale", locale);
        context.setVariable("elements", elements);
        templateHelper.getPayrollData(dto, locale).forEach(context::setVariable);
        return templateEngine.process("payslip/template", context);
    }

    public LinkedList<PayslipElementDto> calculate(PayslipSettingDto dto) {
        Period period = DateUtils.getPayslipPeriod(dto.getPaymentDate());
        List<LocalDate> workingDays = employeeService.getWorkingDays(dto.getEmployee(), period);
        dictionaryService.setVariables(dto, period, workingDays);
        LinkedList<PayslipElementDto> elements = new LinkedList<>();
        PayslipElementDto baseSalaryElement = calculateBaseSalary(dto.getElements());
        double baseSalary = 0d;
        if (baseSalaryElement != null) {
            baseSalary = baseSalaryElement.getGain().doubleValue();
            elements.add(baseSalaryElement);
        }
        dictionaryService.setVariable(BASE_SALARY, baseSalary);
        PayslipElementDto absenceElement = calculateAbsenceElement(dto.getElements());
        if (absenceElement != null) {
            elements.add(absenceElement);
        }

        elements.addAll(calculateSectionsByCategory(dto.getElements(), SectionCategory.GROSS));

        double grossTotal = baseSalary - (absenceElement != null ? absenceElement.getDeduction().doubleValue() : 0) + dictionaryService.findDoubleValue(SectionCategory.GROSS.name());
        dictionaryService.setVariable(GROSS_TOTAL, grossTotal);
        if (dto.isShowIntermediate()) {
            elements.addAll(getTotalElementByCategory(dto.getElements(), GROSS_TOTAL));
        }
        elements.addAll(calculateSectionsByCategory(dto.getElements(), SectionCategory.TAXABLE_CONTRIBUTORY));
        double positionSalary = grossTotal + dictionaryService.findDoubleValue(SectionCategory.TAXABLE_CONTRIBUTORY.name());
        dictionaryService.setVariable(POSITION_SALARY, positionSalary);
        if (dto.isShowIntermediate()) {
            elements.addAll(getTotalElementByCategory(dto.getElements(), POSITION_SALARY));
        }
        elements.addAll(calculateSectionsByCategory(dto.getElements(), SectionCategory.CHARGE));
        double charges = dictionaryService.findDoubleValue(SectionCategory.CHARGE.name());
        dictionaryService.setVariable(SOCIAL_CHARGE, charges);
        if (dto.isShowIntermediate()) {
            elements.addAll(getTotalElementByCategory(dto.getElements(), SOCIAL_CHARGE));
        }
        elements.addAll(calculateSectionsByCategory(dto.getElements(), SectionCategory.TAXABLE_NOT_CONTRIBUTORY));
        double taxableSalary = positionSalary + charges + dictionaryService.findDoubleValue(SectionCategory.TAXABLE_NOT_CONTRIBUTORY.name());
        dictionaryService.setVariable(TAXABLE_SALARY, taxableSalary);
        if (dto.isShowIntermediate()) {
            elements.addAll(getTotalElementByCategory(dto.getElements(), TAXABLE_SALARY));
        }
        elements.addAll(calculateSectionsByCategory(dto.getElements(), SectionCategory.TAXATION));
        double netSalary = taxableSalary  + charges + dictionaryService.findDoubleValue(SectionCategory.TAXATION.name());
        dictionaryService.setVariable(NET_SALARY, netSalary);
        if (dto.isShowIntermediate()) {
            elements.addAll(getTotalElementByCategory(dto.getElements(), NET_SALARY));
        }
        elements.addAll(calculateSectionsByCategory(dto.getElements(), SectionCategory.NOT_TAXABLE_NOT_CONTRIBUTORY));
        double primes = dictionaryService.findDoubleValue(SectionCategory.NOT_TAXABLE_NOT_CONTRIBUTORY.name());
        dictionaryService.setVariable(PRIMES, primes);
        if (dto.isShowIntermediate()) {
            elements.addAll(getTotalElementByCategory(dto.getElements(), PRIMES));
        }
        if (dto.isShowIntermediate()) {
            elements.add(getTotalElement(elements));
        }
        double salary = netSalary + primes;
        dictionaryService.setVariable(SALARY, salary);
        elements.addAll(calculateSectionsByCategory(dto.getElements(), null));
        elements.addAll(getTotalElementByCategory(dto.getElements(), SALARY));
        return elements;
    }

    private PayslipElementDto calculateBaseSalary(List<PayslipSettingElementDto> elements) {
        for (PayslipSettingElementDto element : elements) {
            Section section = sectionRepository.findByCode(element.getSection()).orElse(null);
            if (section != null && section.isSalary()) {
                return calculateSection(element);
            }
        }
        String code = sectionSettingRepository.findByKey(BASE_SALARY).map(SectionSetting::getCode).orElse(null);
        if (code != null) {
            return sectionRepository.findByCode(code)
                    .map(section -> calculateSection(code, section.getLabel(),null, null, null))
                    .orElse(null);
        }
        return null;
    }

    private PayslipElementDto calculateAbsenceElement(List<PayslipSettingElementDto> elements) {
        String code = sectionSettingRepository.findByKey(ABSENCE).map(SectionSetting::getCode).orElse(null);
        if (code != null) {
            boolean alreadyInElements = elements.stream().anyMatch(element -> element.getSection().equals(code));
            if (alreadyInElements) {
                return null;
            }
            return sectionRepository.findByCode(code)
                    .map(section -> calculateSection(code,null , null, null, null))
                    .orElse(null);
        }
        return null;
    }

    private LinkedList<PayslipElementDto> calculateSectionsByCategory(List<PayslipSettingElementDto> elements, SectionCategory category) {
        LinkedList<PayslipElementDto> result = new LinkedList<>();

        for (PayslipSettingElementDto element : elements) {
            Section section = sectionRepository.findByCode(element.getSection()).orElse(null);
            if (section == null) {
                continue;
            }
            boolean isInCategory = false;

            if ((Objects.equals(category, SectionCategory.TAXABLE_CONTRIBUTORY) && section.isContribution() && !section.isSalary()) ||
                    (Objects.equals(category, SectionCategory.TAXABLE_NOT_CONTRIBUTORY) && section.isTaxationNotContribution()) ||
                    (Objects.equals(category, SectionCategory.NOT_TAXABLE_CONTRIBUTORY) && section.isContributionNotTaxation()) ||
                    (Objects.equals(category, SectionCategory.NOT_TAXABLE_NOT_CONTRIBUTORY) && section.notTaxationNotContribution()) ||
                    (Objects.equals(category, SectionCategory.TAXATION) && section.isTaxation()) ||
                    (Objects.equals(category, SectionCategory.CHARGE) && section.isCharge()) ||
                    (Objects.equals(category, SectionCategory.GROSS) && section.isGrossSalary()) ||
                    (category == null && section.getCategory() == null)

            ) {
                isInCategory = true;
            }
            if (!isInCategory) {
                continue;
            }
            PayslipElementDto el = calculateSection(element);
            if (!element.isFix() && el.isZero()) {
                continue;
            }
            result.add(el);
        }
        if (category != null) {
            calculateCategoryTotal(result, category.name());
        }
        return result;
    }

    private List<PayslipElementDto> getTotalElementByCategory(List<PayslipSettingElementDto> elements, String category) {
        if (category == null) {
            return Collections.emptyList();
        }
        String code = sectionSettingRepository.findByKey(category).map(SectionSetting::getCode).orElse(null);
        if (code != null) {
            boolean alreadyInElements = elements.stream().anyMatch(element -> element.getSection().equals(code));
            if (alreadyInElements) {
                return Collections.emptyList();
            }
            double value = dictionaryService.findDoubleValue(category);
            if (value != 0) {
                return sectionRepository.findByCode(code)
                        .map(section -> {
                            PayslipElementDto elementDto = new PayslipElementDto();
                            elementDto.setLabel(section.getLabel());
                            elementDto.setDescription(section.getDescription());
                            elementDto.setTotal(section.isIntermediate());
                            if (value < 0) {
                                elementDto.setDeduction(BigDecimal.valueOf(-value));
                            } else {
                                elementDto.setGain(BigDecimal.valueOf(value));
                            }
                            return Collections.singletonList(elementDto);
                        })
                        .orElse(Collections.emptyList());
            }
        }
        return Collections.emptyList();
    }

    private void calculateCategoryTotal(List<PayslipElementDto> elements, String category) {
        Double gain = elements.stream()
                .map(PayslipElementDto::getGain)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .reduce(0d, Double::sum);
        Double deduction = elements.stream()
                .map(PayslipElementDto::getDeduction)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .reduce(0d, Double::sum);
        dictionaryService.setVariable(category, gain - deduction);
    }

    private PayslipElementDto getTotalElement(List<PayslipElementDto> elements) {
        Double gain = elements.stream()
                .filter(element -> !element.isTotal())
                .map(PayslipElementDto::getGain)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .reduce(0d, Double::sum);
        Double deduction = elements.stream()
                .filter(element -> !element.isTotal())
                .map(PayslipElementDto::getDeduction)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .reduce(0d, Double::sum);
        PayslipElementDto elementDto = new PayslipElementDto();
        elementDto.setLabel("total");
        elementDto.setTotal(true);
        elementDto.setGain(BigDecimal.valueOf(gain));
        elementDto.setDeduction(BigDecimal.valueOf(deduction));
        return elementDto;
    }

    private PayslipElementDto calculateSection(PayslipSettingElementDto element) {
        return calculateSection(element.getSection(), element.getLabel(), element.getBaseValue(), element.getRateValue(), element.getValue());
    }

    private PayslipElementDto calculateSection(String sectionCode, String label,  Double baseValue, Double rateValue, Double value) {
        PayslipElementDto elementDto = new PayslipElementDto();
        sectionRepository.findByCode(sectionCode).ifPresent(section -> {
            elementDto.setCode(section.getCode());
            elementDto.setLabel(label == null ? section.getLabel() : label);
            elementDto.setDescription(section.getDescription());
            elementDto.setTotal(section.isIntermediate());
            Bindings context = new SimpleBindings(dictionaryService.getAllVariables());
            BigDecimal sectionValue;
            if (value == null) {
                BigDecimal base = calculateSectionValue(section.getBaseFormula(), baseValue, context);
                context.put(BASE, base.doubleValue());
                elementDto.setBase(base.equals(ZERO) ? null : base.setScale(2, RoundingMode.HALF_UP));
                BigDecimal rate = calculateSectionValue(section.getRateFormula(), rateValue, context);
                context.put(RATE, section.isRatePercent() ? rate.doubleValue() / 100d : rate.doubleValue());
                elementDto.setRate(rate.equals(ZERO) ? null : rate.setScale(3, RoundingMode.HALF_UP));
                sectionValue = calculateSectionValue(section.getValueFormula(), value, context);
            } else {
                sectionValue = BigDecimal.valueOf(value);
            }
            if (sectionValue == null) {
                return;
            }

            dictionaryService.setVariable(sectionCode, sectionValue.doubleValue());
            BigDecimal gain = null;
            BigDecimal deduction = null;
            if (section.isGain() || section.isSalary()) {
                gain = sectionValue.setScale(2, RoundingMode.HALF_UP);
            } else if (section.isDeduction()) {
                deduction = sectionValue.setScale(2, RoundingMode.HALF_UP);
            } else if (section.isIntermediate()) {
                if (sectionValue.doubleValue() < 0) {
                    deduction = BigDecimal.valueOf(-sectionValue.doubleValue()).setScale(2, RoundingMode.HALF_UP);
                } else {
                    gain = BigDecimal.valueOf(sectionValue.doubleValue()).setScale(2, RoundingMode.HALF_UP);
                }
            }
            elementDto.setGain(gain);
            elementDto.setDeduction(deduction);
        });
        return elementDto;
    }

    private void calculateFormulaVariables(String formula) {
        Pattern pattern = Pattern.compile("[A-Z]\\d{3}", Pattern.MULTILINE);
        Matcher m = pattern.matcher(formula);
        Set<String> sections = new HashSet<>();
        while (m.find()) {
            sections.add(m.group());
        }
        sections.forEach(code -> {
            if (!dictionaryService.hasVariable(code)) {
                calculateSection(code, null, null, null, null);
            }
        });
    }

    private BigDecimal calculateSectionValue(String formula, Double value, Bindings context) {
        if (formula == null && value == null) {
            return ZERO;
        }
        if (formula == null || value != null) {
            return BigDecimal.valueOf(value);
        }
        calculateFormulaVariables(formula);
        try {
            engine.setBindings(context, ScriptContext.GLOBAL_SCOPE);
            Object result = engine.eval(formula);
            if (result instanceof Integer) {
                return BigDecimal.valueOf((int) result);
            } else if (result instanceof Long) {
                return BigDecimal.valueOf((long) result);
            } else if (result instanceof Float) {
                return BigDecimal.valueOf((float) result);
            } else if (result instanceof Double) {
                return BigDecimal.valueOf((double) result);
            }
            return ZERO;
        } catch (ScriptException e) {
            return ZERO;
        }
    }
}
