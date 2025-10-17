package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.RequestContext;
import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.entities.CustomField;
import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionNature;
import com.zsoft.gloody.gloodypayroll.models.repositories.*;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.*;
import com.zsoft.gloody.gloodypayroll.rest.dto.section.SectionDto;
import com.zsoft.gloody.gloodypayroll.rest.mappers.BackPaymentMapper;
import com.zsoft.gloody.gloodypayroll.rest.mappers.CompanyMapper;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapper;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import com.zsoft.gloody.gloodypayroll.utils.TemplateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.*;
import static com.zsoft.gloody.gloodypayroll.configs.RequestContext.*;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PayslipService {
    public static final String ATTRIBUTE_PREFIX = "TOT_";
    public static final String NATURE_PREFIX = "NTR_";
    public static final String CODE_REGEX = "\\b(\\w+)\\s*\\(\\s*([^)]*)\\s*\\)|[A-Z][A-Z0-9_]{2,}";
    public static final String FUNCTION_REGEX = "\\b(\\w+)\\s*\\(\\s*([^)]*)\\s*\\)";
    private final TemplateHelper templateHelper;
    private final SpringTemplateEngine templateEngine;
    private final ScriptEngine engine;
    private final PayslipSettingRepository payslipSettingRepository;
    private static final List<String> defaultFunctionCodes = List.of("irg(base)", "int(base)");
    private static final PayslipSettingElementDto EMPTY_SETTING_ELEMENT = new PayslipSettingElementDto();
    private final PayslipSettingReadOnlyService payslipSettingService;
    private final HRDataRepository hrDataRepository;
    private final VariableService variableService;
    private final EmployeeMapper employeeMapper;
    private final BackPaymentMapper backPaymentMapper;
    private final CompanyMapper companyMapper;
    // TODO CHECK IF THIS STILL NEEDED
    public static final Map<SectionNature, String> SECTION_NATURE_ABBR = new HashMap<>() {
        {
            put(SectionNature.NOT_TAXABLE_CONTRIBUTORY, "NTC");
            put(SectionNature.NOT_TAXABLE_NOT_CONTRIBUTORY, "NTNC");
            put(SectionNature.TAXABLE_NOT_CONTRIBUTORY, "TNC");
            put(SectionNature.TAXABLE_CONTRIBUTORY, "TC");
        }
    };
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final BackPaymentsRepository backPaymentsRepository;


    public Set<Long> getClosedPayslips(LocalDate month) {
        return payslipSettingRepository.getAllClosedPayslipResources(month.withDayOfMonth(1));
    }


    public static void calculatedElementToTemplateElement(PayslipSettingElementDto templateElement, PayslipElementDto calculatedElement) {
        if (calculatedElement == null) {
            templateElement.setValue(0.0);
            templateElement.setBaseValue(0.0);
            templateElement.setRateValue(0.0);
        } else {
            templateElement.setValue(calculatedElement.getGain() != null
                    ? calculatedElement.getGain().doubleValue()
                    : calculatedElement.getDeduction() != null
                    ? calculatedElement.getDeduction().doubleValue()
                    : 0);
            templateElement.setBaseValue(calculatedElement.getBase() != null ? calculatedElement.getBase().doubleValue() : 0.0);
            templateElement.setRateValue(calculatedElement.getRate() != null ? calculatedElement.getRate().doubleValue() : 0.0);
        }
    }

    public PayslipPrintDTO print(PayslipCalculatePayload payload) {
        try {
            EmployeeDto employeeDto = employeeRepository.findById(payload.getResourceId()).map(employeeMapper::toDto).orElse(null);
            if (employeeDto == null) {
                throw new ApiRuntimeException("error.employee.required");
            }
            PayslipController.CalculatedPayslip calculatedPayslip = calculate(payload, null, false);
            if (calculatedPayslip == null || calculatedPayslip.template() == null) {
                return null;
            }
            CompanyDto companyDto = companyRepository.findById(SecurityUtils.getCurrentCompanyId()).map(companyMapper::toDto).orElse(null);
            Locale locale = Locale.getDefault();
            Context context = new Context(locale);
            context.setVariable("locale", locale);
            context.setVariable("elements", calculatedPayslip.calculatedElements());
            templateHelper.getPayrollData(calculatedPayslip.template().getPayslipDetail(), employeeDto, locale, companyDto).forEach(context::setVariable);
            String content = templateEngine.process("payslip/template", context);
            PayslipPrintDTO result = new PayslipPrintDTO();
            result.setContent(content);
            if (Objects.nonNull(calculatedPayslip.template())) {
                result.setShowIntermediate(calculatedPayslip.template().getShowZero());
                result.setShowIntermediate(calculatedPayslip.template().isShowIntermediate());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private PayslipSettingDto mergeTempTemplate(PayslipSettingDto template, PayslipSettingDto tempTemplate) {
        if (tempTemplate == null) {
            return template;
        }
        try {
            PayslipSettingDto newTemplate = (PayslipSettingDto) template.clone();
            newTemplate.setRate(Objects.requireNonNullElse(tempTemplate.getRate(), template.getRate()));
            newTemplate.setShowZero(Objects.requireNonNullElse(tempTemplate.getShowZero(), template.getShowZero()));
            if (tempTemplate.getElements() != null && !tempTemplate.getElements().isEmpty()) {
                newTemplate.setElements(tempTemplate.getElements());
            }
            return newTemplate;
        } catch (Exception ignored) {
        }
        return template;

    }


    private double getCustomFieldValue(String source, Employee employee) {
        if (source == null || source.isEmpty()) {
            return 0d;
        }
        if (employee == null || employee.getCustomFields() == null) {
            return 0d;
        }
        CustomField customField = employee.getCustomFields().stream().filter(c -> Objects.equals(c.getCode(), source)).findFirst().orElse(null);
        return Objects.nonNull(customField) ? customField.getValue() : 0d;
    }

    private Map<String, Double> getMonthlyValues(PayslipCalculatePayload payload, Employee employee) {
        initDictionaryService();
        LocalDate month = Objects.requireNonNullElse(payload.getMonth(), LocalDate.now()).withDayOfMonth(1);
        Map<String, Double> monthlyValues = RequestContext.monthlyValues() != null && RequestContext.monthlyValues().containsKey(payload.getContractId()) ? RequestContext.monthlyValues().get(payload.getContractId())
                : hrDataRepository.findAllByContractIdAndMonth(payload.getContractId(), month).stream()
                .collect(Collectors.toMap(HRMonthlyData::getVariable, HRMonthlyData::getValue, (existing, replacement) -> existing));
        if (monthlyValues.isEmpty()) {
            return Collections.emptyMap();
        }
        Period period = DateUtils.getPayslipPeriod(payload.getMonth());
        Map<String, Double> variables = new HashMap<>();
        dictionaryService().clearAllVariables();
        List<VariableSettingDto> savedVariables = variableService.findAllUnsourcedVariables();
        Map<String, String> variablesMap = savedVariables.stream().filter(variableSettingDto -> Objects.nonNull(variableSettingDto.getSource())).collect(Collectors.toMap(VariableSettingDto::getSource, VariableSettingDto::getKey));
        savedVariables.forEach(variable -> {
            try {
                if (!variables.containsKey(variable.getKey())) {
                    if (variable.getSource() != null) {
                        if (variable.getSource().contains("CUSTOM_FIELD")) {
                            variables.put(variable.getKey(), getCustomFieldValue(variable.getSource(), employee));
                        } else {
                            variables.put(variable.getKey(), monthlyValues.getOrDefault(variable.getSource(), variable.getFormula() == null ? 0 : Double.parseDouble(variable.getFormula())));
                        }
                        monthlyValues.remove(variable.getSource());
                    } else {
                        variables.put(variable.getKey(), Double.valueOf(variable.getFormula()));
                    }
                }
            } catch (Exception e) {
                variables.put(variable.getKey(), 0.0);
            }
        });

        // todo fix holidays
        variables.put("HOLIDAYS", payload.getHolidaysCount());
        variables.putAll(monthlyValues);
        dictionaryService().setVariables(employee, variables, variablesMap);
        dictionaryService().setPeriod(period);
        return variables;
    }

    private void initPerRequestElements(PayslipCalculatePayload payload, PayslipSettingDto template, Long resourceId) {
        perRequestElements().setCalculatedElements(new TreeSet<>((o1, o2) -> Integer.compare(indexOf(o1), indexOf(o2))));
        perRequestElements().setSettingsElements(template.getElements());
        perRequestElements().setShowIntermediate(template.isShowIntermediate());
        perRequestElements().setShowZero(template.getShowZero());
        perRequestElements().setEmployeeId(template.getEmployee().getId());
        perRequestElements().setCurrentDate(template.getPaymentDate());
        perRequestElements().setAdjustBackPayments(payload.isAdjustBackPayments());
        perRequestElements().setResetElementValues(payload.isReCalculate());
        if (RequestContext.functionElements() == null) {
            this.initFunctionElements(Set.of(resourceId));
        }
        perRequestElements().setFunctionElements(RequestContext.functionElements().getOrDefault(resourceId, Collections.emptyList()));
    }

    public void initFunctionElements(Set<Long> resourceIds) {
        List<String> inFunctionsCodes = getInFunctionCodes();
        if (inFunctionsCodes == null || inFunctionsCodes.isEmpty() || resourceIds.isEmpty()) {
            RequestContext.setFunctionElements(Collections.emptyMap());
            return;
        }
        RequestContext.initFunctionElements(resourceIds, inFunctionsCodes);
    }

    public PayslipController.CalculatedPayslip calculate(PayslipCalculatePayload payload, Employee employee, boolean clearContext) {

        try {

            if (employee == null) {
                employee = employeeRepository.findById(payload.getResourceId()).orElse(null);
            }
            if (employee == null) {
                return null;
            }
            Map<String, Double> monthlyValues = getMonthlyValues(payload, employee);
            Long templateId = monthlyValues.getOrDefault("TEMPLATE", -1d).longValue();
            if (payload.getTempTemplate() != null && payload.getTempTemplate().getId() != null) {
                templateId = payload.getTempTemplate().getId();
            }
            PayslipSettingDto template = templateId != -1 ? getTemplate(templateId, false) : getTemplate(templateId, true);
            if (template == null) {
                return null;
            }
            dictionaryService().setRate(template.getRate());
            initRequestContext();
            template = mergeTempTemplate(template, payload.getTempTemplate());
            setBackPayments(template, payload.getContractId(), payload.getMonth().withDayOfMonth(1), payload.getBackPayment());
            engine.setBindings(new SimpleBindings(dictionaryService().getAllVariables()), ScriptContext.GLOBAL_SCOPE);
            dictionaryService().setAddElements(true);
            initPerRequestElements(payload, template, payload.getResourceId());
            for (PayslipSettingElementDto elementDto : template.getElements()) {
                calculateSection(elementDto);
            }
            return new PayslipController.CalculatedPayslip(template, perRequestElements().getCalculatedElements());
        } finally {
            if (clearContext) {
                RequestContext.clear();
            }
        }

    }

    private void resetValues(PayslipSettingElementDto elementDto) {
        if (perRequestElements().isResetElementValues()) {
            elementDto.setValue(null);
            elementDto.setBaseValue(null);
            elementDto.setRateValue(null);
        }
    }

    private int indexOf(PayslipElementDto elementDto) {
        EMPTY_SETTING_ELEMENT.setSection(elementDto.getCode());
        return perRequestElements().getSettingsElements().indexOf(EMPTY_SETTING_ELEMENT);
    }


    private void addFunctionCodes(List<String> codes, String formula) {
        if (formula == null) {
            return;
        }

        Pattern pattern = Pattern.compile(FUNCTION_REGEX);
        Matcher matcher = pattern.matcher(formula);
        while (matcher.find()) {
            String params = matcher.group(2);

            if (!params.isEmpty()) {
                String[] paramList = params.split("\\s*,\\s*"); // Split parameters by comma
                for (String param : paramList) {
                    if (companySections().getSections().containsKey(param)) {
                        codes.add(param);
                    }
                }
            }
        }

    }


    public List<String> getInFunctionCodes() {
        List<String> codes = new ArrayList<>();
        companySections().getSections().values().forEach(section -> {
                    addFunctionCodes(codes, section.getBaseFormula());
                    addFunctionCodes(codes, section.getValueFormula());
                    addFunctionCodes(codes, section.getBaseFormula());
                }
        );
        return codes;
    }

    private void calculateByLabel(String prefix, String label) {
        if (prefix == null || (!prefix.equals(ATTRIBUTE_PREFIX) && !prefix.equals(NATURE_PREFIX))) {
            return;
        }
        for (PayslipSettingElementDto element : perRequestElements().getSettingsElements()) {
            SectionDto section = companySections().findByCode(element.getSection()).orElse(null);
            if (section == null) {
                continue;
            }
            boolean isInCategory = prefix.equals(ATTRIBUTE_PREFIX) ? section.getLabelKeys() != null && section.getLabelKeys().contains(label) :
                    Objects.equals(label, SECTION_NATURE_ABBR.get(section.getNature()));
            if (!isInCategory) {
                continue;
            }
            PayslipElementDto el = calculateSection(element);
            if (Objects.isNull(el)) {
                return;
            }
            if (el.isTotal() || section.isSalary()) {
                continue;
            }
            updateTotal(prefix, label, section.isGain(), BigDecimal.valueOf(dictionaryService().findDoubleValue(section.getCode())));
        }
        updateTotal(prefix, label, true, BigDecimal.ZERO);
    }

    private void updateTotal(String prefix, String label, boolean gain, BigDecimal value) {
        String key = String.format(prefix + "%s", label);
        BigDecimal recentTotal = BigDecimal.valueOf(dictionaryService().findDoubleValue(key));
        dictionaryService().setVariable(key, gain ? recentTotal.add(value) : recentTotal.subtract(value));
    }

    private PayslipElementDto getTotalElement(String label) {
        Double gain = perRequestElements().getCalculatedElements().stream()
                .filter(element -> !element.isTotal())
                .map(PayslipElementDto::getGain)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .reduce(0d, Double::sum);
        Double deduction = perRequestElements().getCalculatedElements().stream()
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
        try {
            if (dictionaryService().getAddElements()) {
                PayslipElementDto alreadyExistsElement = perRequestElements().getCalculatedElements().stream().filter(e -> Objects.equals(e.getCode(), element.getSection())).findFirst().orElse(null);
                if (alreadyExistsElement != null) {
                    return alreadyExistsElement;
                }
            }
            resetValues(element);
            return calculateSection(element.getSection(), element.getLabel(), element.getBaseValue(), element.getRateValue(), element.getValue());
        } catch (Exception e) {
            log.error("ERROR WHILE CALCULATING ELEMENT {}", element.getSection(), e);
        }
        return null;
    }

    private boolean isHidden(PayslipElementDto element) {
        return (element.isTotal() && !perRequestElements().isShowIntermediate()) ||
                (!perRequestElements().isShowZero() && element.isZero());
    }

    private void adjustBackPayments(PayslipSettingElementDto element, PayslipElementDto dto) {

        if (Objects.isNull(element) || Objects.isNull(element.getBackPayments()) || element.getBackPayments().isEmpty()) {
            return;
        }
        double backPayments = element.getBackPayments().stream().mapToDouble(BackPaymentDto::getValue).sum();
        double currentValue = dictionaryService().findDoubleValue(element.getSection());
        if (!element.isFix() || perRequestElements().isAdjustBackPayments()) {
            dto.setValue(currentValue + backPayments);
        }
        if (perRequestElements().isAdjustBackPayments() && !dto.isTotal()) {
            dictionaryService().setVariable(element.getSection(), currentValue + backPayments);
        }

    }

    public void addCalculatedElement(PayslipElementDto element) {
        EMPTY_SETTING_ELEMENT.setSection(element.getCode());
        if (!perRequestElements().getSettingsElements().contains(EMPTY_SETTING_ELEMENT)) {
            return;
        }
        PayslipSettingElementDto settingElementDto = perRequestElements().getSettingsElements().stream().filter(el -> el.getSection().equals(element.getCode())).findFirst().orElse(null);
        if (settingElementDto == null) {
            return;
        }
        adjustBackPayments(settingElementDto, element);
        if (!isHidden(element)) {
            element.setBackPayments(settingElementDto.getBackPayments());
            perRequestElements().getCalculatedElements().add(element);
        }
    }

    private PayslipElementDto evaluateSectionFormulas(SectionDto section, String label, Double baseValue, Double rateValue, Double value) {
        AtomicReference<PayslipElementDto> elementDto = new AtomicReference<>(new PayslipElementDto());
        elementDto.get().setCode(section.getCode());
        elementDto.get().setIdentifier(SECTIONS_PREFIX + section.getIdentifier());
        elementDto.get().setLabel(label == null ? section.getLabel() : label);
        elementDto.get().setDescription(section.getDescription());
        elementDto.get().setTotal(section.isIntermediate());
        elementDto.get().setRatePercent(section.isRatePercent());
        Bindings context = new SimpleBindings();
        BigDecimal sectionValue;
        BigDecimal base = calculateSectionValue(section.getBaseFormula(), baseValue, context, section.getNature());
        context.put(BASE, base.doubleValue());
        elementDto.get().setBase(base.equals(ZERO) ? null : base.setScale(3, RoundingMode.DOWN));
        BigDecimal rate = calculateSectionValue(section.getRateFormula(), rateValue, context, section.getNature());
        context.put(RATE, section.isRatePercent() ? rate.doubleValue() / 100d : rate.doubleValue());
        elementDto.get().setRate(rate.equals(ZERO) ? null : rate.setScale(3, RoundingMode.DOWN));
        if (value == null) {
            sectionValue = calculateSectionValue(section.getValueFormula(), value, context, section.getNature());
        } else {
            sectionValue = BigDecimal.valueOf(value);
        }

        if (sectionValue == null) {
            return elementDto.get();
        }
        dictionaryService().setVariable(section.getCode(), sectionValue.doubleValue());
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
        return elementDto.get();
    }


    private PayslipElementDto calculateSection(String code, String label, Double baseValue, Double rateValue, Double value) {
        AtomicReference<PayslipElementDto> elementDto = new AtomicReference<>(new PayslipElementDto());
        if (code.startsWith(ATTRIBUTE_PREFIX)) {
            calculateByLabel(ATTRIBUTE_PREFIX, code.replace(ATTRIBUTE_PREFIX, ""));
            return null;
        } else if (code.startsWith(NATURE_PREFIX)) {
            calculateByLabel(NATURE_PREFIX, code.replace(NATURE_PREFIX, ""));
            return null;
        } else {
            companySections().findByCode(code).ifPresent(section -> {
                if (section.isTotal()) {
                    elementDto.set(getTotalElement(label == null ? section.getLabel() : label));
                    elementDto.get().setCode(section.getCode());
                    elementDto.get().setIdentifier(SECTIONS_PREFIX + section.getIdentifier());
                    if (dictionaryService().getAddElements()) {
                        addCalculatedElement(elementDto.get());
                    }
                    return;
                }
                boolean hasOwnVariables = section.getVariables() != null && !section.getVariables().isEmpty();
                if (hasOwnVariables) {

                    LinkedHashMap<String, Object> currentVariables = new LinkedHashMap<>(dictionaryService().getAllVariables()); // first we need to save the current variables state
                    boolean currentAddElement = dictionaryService().getAddElements();
                    LinkedHashMap<String, String> currentVariableValues = new LinkedHashMap<>(10);
                    section.getVariables().forEach(sectionVariable -> currentVariableValues.put(sectionVariable.getVariableKey(), String.valueOf(dictionaryService().getInitialVariables().getOrDefault(sectionVariable.getVariableKey(), "0"))));
                    dictionaryService().setAddElements(false);
                    dictionaryService().getAllVariables().clear();
                    section.getVariables().forEach(sectionVariable -> dictionaryService().getInitialVariables().put(sectionVariable.getVariableKey(), Double.valueOf(sectionVariable.getValue())));
                    dictionaryService().getAllVariables().putAll(dictionaryService().getInitialVariables());
                    elementDto.set(evaluateSectionFormulas(section, label, baseValue, rateValue, value));
                    Double calculatedValue = dictionaryService().findDoubleValue(section.getCode());
                    //reset variables state
                    dictionaryService().getAllVariables().clear();
                    section.getVariables().forEach(sectionVariable -> dictionaryService().getInitialVariables().put(sectionVariable.getVariableKey(), currentVariableValues.getOrDefault(sectionVariable.getVariableKey(), "0")));
                    dictionaryService().getAllVariables().putAll(currentVariables);
                    dictionaryService().getAllVariables().putAll(dictionaryService().getInitialVariables());
                    dictionaryService().setVariable(section.getCode(), Objects.requireNonNullElse(calculatedValue, 0.0));
                    dictionaryService().setAddElements(currentAddElement);
                } else {
                    elementDto.set(evaluateSectionFormulas(section, label, baseValue, rateValue, value));
                }
                if (dictionaryService().getAddElements()) {
                    addCalculatedElement(elementDto.get());
                }
            });
        }

        return elementDto.get();
    }

    private Period getFunctionPeriod(String functionName, String[] paramList) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        int n = Integer.parseInt(paramList[1]);
        n = perRequestElements().getCurrentDate().getMonthValue() < 7 ? n + 1 : n;
        LocalDate targetYear = perRequestElements().getCurrentDate().withMonth(7).withDayOfMonth(1).minusYears(n);
        if (functionName.contains("APD")) {
            int nbrOfMonths = Integer.parseInt(paramList[2]);
            startDate = targetYear;
            endDate = startDate.plusMonths(nbrOfMonths).minusMonths(1);
        } else if (functionName.contains("BK")) {
            int nbrOfMonths = Integer.parseInt(paramList[2]);
            endDate = targetYear.plusMonths(11);
            startDate = endDate.minusMonths(nbrOfMonths).plusMonths(1);
        } else {
            int parsedStartMonth = Integer.parseInt(paramList[2]);
            int parsedEndMonth = Integer.parseInt(paramList[3]);
            startDate = parsedStartMonth < 7 ? targetYear.plusYears(1).withMonth(parsedStartMonth)
                    : targetYear.withMonth(parsedEndMonth);
            endDate = parsedEndMonth < 7 ? targetYear.plusYears(1).withMonth(parsedEndMonth)
                    : targetYear.withMonth(parsedEndMonth);
        }
        return new Period(startDate, endDate);
    }

    private Double calculateFunctionByName(String functionName, String[] paramList) {
        if (Objects.equals(functionName, "EOFMONTH")) {
            return perRequestElements().getCurrentDate().isEqual(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())) ? 1.0 : 0.0;
        } else {
            Period period = getFunctionPeriod(functionName, paramList);
            if (period.end().isBefore(period.start()) || period.end().isEqual(period.start())) {
                return 0.0;
            }
            DoubleStream values = perRequestElements().getFunctionElements()
                    .stream().filter(el -> el.code().equals(paramList[0]) && DateUtils.isDateBetween(el.date(), period.start(), period.end()))
                    .mapToDouble(PayslipFunctionElement::value);
            if (functionName.contains("MOY")) {
                return values.average().orElse(0.0);
            } else if (functionName.contains("MAX")) {
                return values.max().orElse(0.0);
            } else if (functionName.contains("MIN")) {
                return values.min().orElse(0.0);
            } else
                return values.min().orElse(0.0);
        }

    }

    private void calculateFunction(String function) {
        Pattern pattern = Pattern.compile(FUNCTION_REGEX);
        Matcher matcher = pattern.matcher(function);
        while (matcher.find()) {
            String functionName = matcher.group(1);
            String params = matcher.group(2);
            Double value = calculateFunctionByName(functionName, params.isEmpty() ? null : params.split("\\s*,\\s*"));
            dictionaryService().setVariable(function, value);
        }

    }

    private boolean isFunction(String value) {
        return defaultFunctionCodes.stream().noneMatch((func) -> value.toLowerCase().contains(func)) && value.matches(FUNCTION_REGEX);
    }

    private void calculateFormulaVariables(String formula) {
        Pattern pattern = Pattern.compile(CODE_REGEX, Pattern.MULTILINE);
        Matcher m = pattern.matcher(formula);
        Set<String> sections = new HashSet<>();
        while (m.find()) {
            sections.add(m.group());
        }
        for (String code : sections) {
            if (!dictionaryService().hasVariable(code)) {
                if (isFunction(code)) {
                    calculateFunction(code);
                } else if (!code.matches(FUNCTION_REGEX)) {
                    calculateSection(code, null, null, null, null);
                }
            }
        }
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
            Pattern pattern = Pattern.compile(FUNCTION_REGEX);
            Matcher matcher = pattern.matcher(formula);
            while (matcher.find()) {
                if (isFunction(matcher.group())) {
                    formula = formula.replace(matcher.group(), String.valueOf(dictionaryService().findDoubleValue(matcher.group())));
                }
            }
            formula = formula.replace("INT(BASE)", "parseInt(BASE)");
            engine.getBindings(ScriptContext.GLOBAL_SCOPE).putAll(context);
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
        } catch (Exception e) {
            return ZERO;
        }
    }

    public PayslipDetailDto getPayslipDetails(PayslipCalculatePayload payload) {
        if (payslipSettingRepository.isClosedPayslip(payload.getMonth().withDayOfMonth(1), payload.getContractId())) {
            Long templateId = hrDataRepository.findTopHrMonthlyDataByVariableAndContractIdAndMonth("TEMPLATE", payload.getContractId(), payload.getMonth().withDayOfMonth(1)).map(HRMonthlyData::getValue).orElse(-1d).longValue();
            PayslipSettingDto template = templateId != -1 ? getTemplate(templateId, false) : getTemplate(templateId, true);
            if (template == null) {
                return null;
            }
            if (template.getPayslipDetail() != null) {
                return template.getPayslipDetail();
            }
        }
        Employee employee = employeeRepository.findById(payload.getResourceId()).orElse(null);
        return employeeMapper.employeeToPayslipDetails(employee, payload.getContractId(), payload.getMonth().withDayOfMonth(1));
    }

    private PayslipSettingDto getTemplate(Long id, boolean isDefault) {

        if (isDefault) {
            return RequestContext.defaultTemplate() != null ? RequestContext.defaultTemplate() : payslipSettingService.findDefaultTemplate().orElse(null);
        } else {
            return RequestContext.templates() != null && RequestContext.templates().containsKey(id) ? RequestContext.templates().get(id) : payslipSettingService.findById(id).orElse(null);
        }

    }


    private void setBackPayment(PayslipSettingDto template, BackPaymentDto backPaymentDto) {
        if (template == null || template.getElements() == null || backPaymentDto == null) {
            return;
        }
        template.getElements().forEach(element -> element.setBackPayments(null));
        template.getElements().stream().filter(el -> Objects.equals(el.getSection(), backPaymentDto.getSectionCode())).forEach(element -> element.setBackPayments(List.of(backPaymentDto)));
    }

    public void setBackPayments(PayslipSettingDto template, Long contractId, LocalDate month, BackPaymentDto backPaymentDto) {
        if (template == null || template.getElements() == null) {
            return;
        }
        setBackPayment(template, backPaymentDto);
        Map<String, List<BackPaymentDto>> backPayments = backPaymentsRepository.findAllByContractIdAndMonthAndSectionCodeIsNotNullAndSourceIsNotNull(contractId, month).stream().map(backPaymentMapper::toDto).collect(Collectors.groupingBy(BackPaymentDto::getSectionCode));
        if (backPayments.isEmpty()) {
            return;
        }
        template.getElements().forEach(element -> element.setBackPayments(backPayments.getOrDefault(element.getSection(), null)));
    }

}
