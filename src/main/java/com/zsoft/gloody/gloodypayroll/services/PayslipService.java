package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.beans.CompanySections;
import com.zsoft.gloody.gloodypayroll.models.beans.DefaultSectionSettings;
import com.zsoft.gloody.gloodypayroll.models.beans.PerRequestElements;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.entities.Section;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionNature;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.*;

@Service
@AllArgsConstructor
public class PayslipService {
    private final CompanySections companySections;
    private final SectionDictionaryService dictionaryService;
    private final DefaultSectionSettings defaultSectionSettings;
    private final TemplateHelper templateHelper;
    private final SpringTemplateEngine templateEngine;
    private ScriptEngine engine;
    private final PerRequestElements perRequestElements;
    private static final PayslipSettingElementDto EMPTY_SETTING_ELEMENT = new PayslipSettingElementDto();
    public static final Map<SectionNature, String> SECTION_NATURE_ABBR = new HashMap<>() {
        {
            put(SectionNature.NOT_TAXABLE_CONTRIBUTORY, "NTC");
            put(SectionNature.NOT_TAXABLE_NOT_CONTRIBUTORY, "NTNC");
            put(SectionNature.TAXABLE_NOT_CONTRIBUTORY, "TNC");
            put(SectionNature.TAXABLE_CONTRIBUTORY, "TC");
        }
    };

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
        Set<PayslipElementDto> elements = calculate(dto);
        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        context.setVariable("locale", locale);
        context.setVariable("elements", elements);
        templateHelper.getPayrollData(dto, locale).forEach(context::setVariable);
        return templateEngine.process("payslip/template", context);
    }

    public TreeSet<PayslipElementDto> calculate(PayslipSettingDto dto) {
        Period period = DateUtils.getPayslipPeriod(dto.getPaymentDate());
        dictionaryService.setVariables(dto, period);
        TreeSet<PayslipElementDto> calculatedElements = new TreeSet<>((o1, o2) -> Integer.compare(indexOf(o1), indexOf(o2)));
        perRequestElements.setCalculatedElements(calculatedElements);
        perRequestElements.setSettingsElements(dto.getElements());
        perRequestElements.setShowIntermediate(dto.isShowIntermediate());
        PayslipElementDto baseSalaryElement = calculateBaseSalary(dto.getElements());
        double baseSalary = 0d;
        if (baseSalaryElement != null) {
            baseSalary = baseSalaryElement.getGain().doubleValue();
        }
        dictionaryService.setVariable(BASE_SALARY, baseSalary);

        for(PayslipSettingElementDto elementDto: dto.getElements()) { // ignore base salary
               calculateSection( elementDto);
        }
        /*PayslipElementDto absenceElement = calculateAbsenceElement(dto.getElements());
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

         */
        return   perRequestElements.getCalculatedElements();
    }

    private int indexOf(PayslipElementDto elementDto) {
        EMPTY_SETTING_ELEMENT.setSection(elementDto.getCode());
        return perRequestElements.getSettingsElements().indexOf(EMPTY_SETTING_ELEMENT);
    }

    private PayslipElementDto calculateBaseSalary(List<PayslipSettingElementDto> elements) {
        for (PayslipSettingElementDto element : elements) {
            Section section = companySections.findByCode(element.getSection()).orElse(null);
            if (section != null && section.isSalary()) {
                return calculateSection(element);
            }
        }
        String code = defaultSectionSettings.findByKey(BASE_SALARY).map(SectionSetting::getCode).orElse(null);
        if (code != null) {
            return companySections.findByCode(code)
                    .map(section -> calculateSection(code, section.getLabel(),null, null, null))
                    .orElse(null);
        }
        return null;
    }


    private void calculateSectionsByNature(String category) {
        for (PayslipSettingElementDto element :   perRequestElements.getSettingsElements()) {
            Section section = companySections.findByCode(element.getSection()).orElse(null);
            if (section == null || section.getNature() == null) {
                continue;
            }
            boolean isInCategory = Objects.equals(category, SECTION_NATURE_ABBR.get(section.getNature()));
            if (!isInCategory) {
                continue;
            }
            PayslipElementDto el = calculateSection(element);
            if (el.isTotal() || section.isSalary()) {
                continue;
            }
            updateCategoryTotal(category, el.getGain(), el.getDeduction());
        }
        updateCategoryTotal(category, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private List<PayslipElementDto> getTotalElementByCategory(List<PayslipSettingElementDto> elements, String category) {
        if (category == null) {
            return Collections.emptyList();
        }
        String code = defaultSectionSettings.findByKey(category).map(SectionSetting::getCode).orElse(null);
        if (code != null) {
            boolean alreadyInElements = elements.stream().anyMatch(element -> element.getSection().equals(code));
            if (alreadyInElements) {
                return Collections.emptyList();
            }
            double value = dictionaryService.findDoubleValue(category);
            if (value != 0) {
                return companySections.findByCode(code)
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

    private void updateCategoryTotal(String category, BigDecimal gain, BigDecimal deduction) {
        BigDecimal recentTotal = BigDecimal.valueOf(dictionaryService.findDoubleValue(String.format("NTR_%s",category)));
        dictionaryService.setVariable(String.format("NTR_%s",category), recentTotal.add( (gain != null ? gain : BigDecimal.ZERO) ).subtract((deduction != null ? deduction : BigDecimal.ZERO)) );
    }

    private PayslipElementDto getTotalElement( String label) {
        Double gain = perRequestElements.getCalculatedElements().stream()
                .filter(element -> !element.isTotal())
                .map(PayslipElementDto::getGain)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .reduce(0d, Double::sum);
        Double deduction = perRequestElements.getCalculatedElements().stream()
                .filter(element -> !element.isTotal())
                .map(PayslipElementDto::getDeduction)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .reduce(0d, Double::sum);
        PayslipElementDto elementDto = new PayslipElementDto();
        elementDto.setLabel(label);
        elementDto.setTotal(true);
        elementDto.setGain(BigDecimal.valueOf(gain));
        elementDto.setDeduction(BigDecimal.valueOf(deduction));
        return elementDto;
    }

    private PayslipElementDto calculateSection(PayslipSettingElementDto element) {
        PayslipElementDto alreadyExistsElement = perRequestElements.getCalculatedElements().stream().filter(e -> e.getCode().contains(element.getSection())).findFirst().orElse(null);
        if (alreadyExistsElement != null) {
            return alreadyExistsElement;
        }
        return calculateSection(element.getSection(), element.getLabel(), element.getBaseValue(), element.getRateValue(), element.getValue());
    }

    public void addCalculatedElement( PayslipElementDto element) {
        EMPTY_SETTING_ELEMENT.setSection(element.getCode());
        if (!perRequestElements.getSettingsElements().contains(EMPTY_SETTING_ELEMENT)) {
            return;
        }
        if ((perRequestElements.isShowIntermediate() && element.isTotal()) || !element.isTotal() ) {
            perRequestElements.getCalculatedElements().add(element);
        }
    }


    private PayslipElementDto calculateSection(String code, String label,  Double baseValue, Double rateValue, Double value) {
        AtomicReference<PayslipElementDto> elementDto = new AtomicReference<>(new PayslipElementDto());
        if (code.startsWith("NTR_")) {
            calculateSectionsByNature(code.replace("NTR_", ""));
            return null;
        } else {
            companySections.findByCode(code).ifPresent(section -> {
                if (section.isTotal()) {
                    elementDto.set(getTotalElement(label == null ? section.getLabel() : label));
                    elementDto.get().setCode(code);
                    addCalculatedElement(elementDto.get());
                    return;
                }
                elementDto.get().setCode(code);
                elementDto.get().setLabel(label == null ? section.getLabel() : label);
                elementDto.get().setDescription(section.getDescription());
                elementDto.get().setTotal(section.isIntermediate());
                Bindings context = new SimpleBindings(dictionaryService.getAllVariables());
                BigDecimal sectionValue;
                    BigDecimal base = calculateSectionValue(section.getBaseFormula(), baseValue, context, section.getNature());
                    context.put(BASE, base.doubleValue());
                    elementDto.get().setBase(base.equals(ZERO) ? null : base.setScale(2, RoundingMode.HALF_UP));
                    BigDecimal rate = calculateSectionValue(section.getRateFormula(), rateValue, context, section.getNature());
                    context.put(RATE, section.isRatePercent() ? rate.doubleValue() / 100d : rate.doubleValue());
                    elementDto.get().setRate(rate.equals(ZERO) ? null : rate.setScale(3, RoundingMode.HALF_UP));
                if (value == null) {
                    sectionValue = calculateSectionValue(section.getValueFormula(), value, context, section.getNature());
                } else {
                    sectionValue = BigDecimal.valueOf(value);
                }
                if (sectionValue == null) {
                    return;
                }

                dictionaryService.setVariable(code, sectionValue.doubleValue());
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
                elementDto.get().setGain(gain);
                elementDto.get().setDeduction(deduction);
                addCalculatedElement(elementDto.get());
            });
        }

        return elementDto.get();
    }

    private void calculateFormulaVariables(String formula) {
        Pattern pattern = Pattern.compile("(w+)|(NTR_[A-Z]+)", Pattern.MULTILINE);
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

    private BigDecimal calculateSectionValue(String formula, Double value, Bindings context, SectionNature nature) {
        if (formula == null && value == null) {
            return ZERO;
        }
        if (formula == null || value != null) {
            return BigDecimal.valueOf(value);
        }
        if (nature != null && formula.contains(SECTION_NATURE_ABBR.get(nature))) {
            return ZERO;
        }
        calculateFormulaVariables(formula);
        try {
            engine.setBindings(context, ScriptContext.GLOBAL_SCOPE);
            Object result = engine.eval(formula);
            if (result instanceof String) {
                return new BigDecimal(String.valueOf(result));
            }
            if (result instanceof BigDecimal) {
                return (BigDecimal) result;
            }
            if (result instanceof Integer) {
                return BigDecimal.valueOf((int) result);
            }
            if (result instanceof Long) {
                return BigDecimal.valueOf((long) result);
            }
            if (result instanceof Float) {
                return BigDecimal.valueOf((float) result);
            }
            if (result instanceof Double) {
                return BigDecimal.valueOf((double) result);
            }
            return ZERO;
        } catch (ScriptException e) {
            return ZERO;
        }
    }
}
