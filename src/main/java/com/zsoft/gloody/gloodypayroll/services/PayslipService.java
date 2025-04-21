package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.RequestContext;
import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import com.zsoft.gloody.gloodypayroll.models.entities.*;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionNature;
import com.zsoft.gloody.gloodypayroll.models.repositories.EmployeeRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.controllers.PayslipController;
import com.zsoft.gloody.gloodypayroll.rest.dto.*;
import com.zsoft.gloody.gloodypayroll.rest.mappers.EmployeeMapper;
import com.zsoft.gloody.gloodypayroll.rest.mappers.PayslipSettingMapper;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import com.zsoft.gloody.gloodypayroll.utils.TemplateHelper;
import lombok.RequiredArgsConstructor;
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
public class PayslipService {
    public static final String ATTRIBUTE_PREFIX = "LBL_";
    public static final String NATURE_PREFIX = "NTR_";
    public static final String CODE_REGEX = "\\b(\\w+)\\s*\\(\\s*([^)]*)\\s*\\)|[A-Z][A-Z0-9_]{2,}";
    public static final String FUNCTION_REGEX = "\\b(\\w+)\\s*\\(\\s*([^)]*)\\s*\\)";
    private final TemplateHelper templateHelper;
    private final SpringTemplateEngine templateEngine;
    private final PayslipElementSettingRepository payslipElementSettingRepository;
    private final ScriptEngine engine;
    private final PayslipSettingRepository payslipSettingRepository;
    private final PayslipSettingMapper payslipSettingMapper;
    private static final List<String> defaultFunctionCodes = List.of("irg(base)", "int(base)");
    private static final PayslipSettingElementDto EMPTY_SETTING_ELEMENT = new PayslipSettingElementDto();
    private final PayslipSettingService payslipSettingService;
    private final HRDataRepository hrDataRepository;
    private final VariableService variableService;
    private final EmployeeMapper employeeMapper;
    public static final Map<SectionNature, String> SECTION_NATURE_ABBR = new HashMap<>() {
        {
            put(SectionNature.NOT_TAXABLE_CONTRIBUTORY, "NTC");
            put(SectionNature.NOT_TAXABLE_NOT_CONTRIBUTORY, "NTNC");
            put(SectionNature.TAXABLE_NOT_CONTRIBUTORY, "TNC");
            put(SectionNature.TAXABLE_CONTRIBUTORY, "TC");
        }
    };
    private final EmployeeRepository employeeRepository;
    private final PayslipDraftService payslipDraftService;

    public List<Long> closePayslips(Set<Long> resourceIds, LocalDate month) {
        List<PayslipSettingDto> result = new ArrayList<>();
        month = month.withDayOfMonth(1);
        List<Long> failedIds = new ArrayList<>();
        if (resourceIds.isEmpty()) {
            return failedIds;
        }
        Map<Long, List<Long>> contracts = loadContracts(resourceIds, month);
        if (contracts.isEmpty()) {
            return failedIds;
        }
        if (contracts.size() > 20) {
            RequestContext.initTemplates();
        }
        RequestContext.initMonthlyValues(contracts.keySet());
        Set<Long> closedContractIds = new HashSet<>();
        Map<Long, Employee> employeeMap = employeeRepository.findAllById(contracts.keySet()).stream().collect(Collectors.toMap(Employee::getId, em -> em));
        PayslipController.PayslipCalculatePayload payload = new PayslipController.PayslipCalculatePayload();
        for (Long resourceId : resourceIds) {
            try {
                if (contracts.containsKey(resourceId)) {
                    Set<Long> resourceContracts = new HashSet<>(contracts.get(resourceId));
                    for (Long contractId : resourceContracts) {
                        payload.setValues(resourceId, contractId, month);
                        PayslipController.CalculatedPayslip calculatedPayslip = calculate(payload, employeeMap.getOrDefault(resourceId, null), false, false);
                        PayslipSettingDto payslip = calculatedPayslip.template();
                        Long payslipId = payslip.getId();
                        boolean isDefault = payslip.isTemplate() || payslip.isByDefault();
                        if (isDefault) {
                            payslip = (PayslipSettingDto) payslip.clone();
                            payslip.setId(null);
                        }
                        Map<String, PayslipElementDto> calculatedElementsMap = calculatedPayslip.calculatedElements() != null ? calculatedPayslip.calculatedElements().stream().collect(Collectors.toMap(PayslipElementDto::getCode, el -> el)) : Collections.emptyMap();
                        payslip.setElements(payslip.getElements().stream().map(e -> {
                            PayslipElementDto element = calculatedElementsMap.getOrDefault(e.getSection(), null);
                            calculatedElementToTemplateElement(e, element);
                            e.setPayslipSettingId(isDefault ? null : payslipId);
                            e.setId(isDefault ? null : e.getId());
                            return e;
                        }).toList());

                        payslip.setClosed(true);
                        payslip.setTemplate(false);
                        payslip.setDraft(false);
                        payslip.setContract(ContractDto.builder().id(contractId).build());
                        payslip.setByDefault(false);
                        payslip.setName(String.format("%s-%s-%s", resourceId, contractId, month));
                        payslip.setPaymentDate(month);
                        payslip.setEmployee(EmployeeDto.builder().id(resourceId).build());
                        if (payslip.getPayslipDetail() == null) {
                            payslip.setPayslipDetail(employeeMapper.employeeToPayslipDetails(employeeMap.getOrDefault(resourceId, null), contractId, month));
                        }
                        result.add(payslip);
                        closedContractIds.add(contractId);
                    }
                }
            } catch (Exception e) {
                failedIds.add(resourceId);
            }
        }
        RequestContext.clear();
        List<PayslipSetting> closedPayslips = payslipSettingRepository.saveAll(result.stream().map(payslipSettingMapper::toEntity).toList());
        hrDataRepository.deleteAllByVariableAndContractIdInAndMonth("TEMPLATE", closedContractIds, month);
        hrDataRepository.bulkInsertClosedTemplateIds(month, closedPayslips.stream().map(PayslipSetting::getId).toList());
        payslipDraftService.deleteAllDraftedByContractAndMonth(closedContractIds, month);
        return failedIds;
    }

    public PayslipElementDto templateElementToCalculatedElement(PayslipSettingElementDto templateElement) {
        PayslipElementDto calculatedElement = new PayslipElementDto();
        SectionDto sectionDto =  companySections().findByCode(templateElement.getSection()).get();
        calculatedElement.setCode(sectionDto.getCode());
        calculatedElement.setTotal(sectionDto.isIntermediate());
        calculatedElement.setDescription(sectionDto.getDescription());
        calculatedElement.setLabel(sectionDto.getLabel());
        calculatedElement.setBase(BigDecimal.valueOf(templateElement.getBaseValue()));
        calculatedElement.setRate(BigDecimal.valueOf(templateElement.getRateValue()));
        calculatedElement.setGain(templateElement.getValue() > 0 ? BigDecimal.valueOf(templateElement.getValue()) : null);
        calculatedElement.setDeduction(templateElement.getValue() < 0 ? BigDecimal.valueOf(templateElement.getValue()) : null);
        return calculatedElement;
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

    public String print(PayslipController.PayslipCalculatePayload payload) {
        EmployeeDto employeeDto = employeeRepository.findById(payload.getResourceId()).map(employeeMapper::toDto).orElse(null);
        if (employeeDto == null) {
            throw new ApiRuntimeException("error.employee.required");
        }
        PayslipController.CalculatedPayslip calculatedPayslip = calculate(payload, null, false, false);
        if (calculatedPayslip == null || calculatedPayslip.template() == null) {
            return null;
        }
        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        context.setVariable("locale", locale);
        context.setVariable("elements", calculatedPayslip.calculatedElements());
        templateHelper.getPayrollData(calculatedPayslip.template().getPayslipDetail(), employeeDto, locale).forEach(context::setVariable);
        return templateEngine.process("payslip/template", context);
    }

    private PayslipSettingDto mergeTempTemplate(PayslipSettingDto template, PayslipSettingDto tempTemplate) {
        if (tempTemplate == null) {
            return template;
        }
        try {
            PayslipSettingDto newTemplate = (PayslipSettingDto) template.clone();
            newTemplate.setRate(Objects.requireNonNullElse(tempTemplate.getRate(), template.getRate()));
            newTemplate.setShowZero(tempTemplate.isShowZero());
            if (tempTemplate.getElements() != null && !tempTemplate.getElements().isEmpty()) {
                newTemplate.setElements(tempTemplate.getElements());
            }
            return newTemplate;
        } catch (Exception ignored) {}
        return template;

    }

    private Map<Long, List<Long>> loadContracts(Set<Long> resourceIds, LocalDate month) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return hrDataRepository.getContractIdsByMonthAndResourceIdIn(month.withDayOfMonth(1), resourceIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(0, Long.class),
                        Collectors.mapping(
                                tuple -> tuple.get(1, Long.class),
                                Collectors.toCollection(ArrayList::new)
                        )
                ));
    }

    public LinkedHashSet<PayslipElementDto> loadDraftedIfPresent(PayslipController.PayslipCalculatePayload payload, Map<String, Double > monthlyValues, PayslipSettingDto payslipTemplate) {
        LinkedHashSet<PayslipElementDto> calculatedElements = new LinkedHashSet<>();
        if (payslipTemplate.isClosed()) {
            payslipTemplate.getElements().forEach(el -> calculatedElements.add(templateElementToCalculatedElement(el)));
        } else {
            if (isTemp(payload)) {
                return null;
            }
            if (!monthlyValues.containsKey("DRAFT_ID")) {
                return null;
            }
            Long draftId = monthlyValues.get("DRAFT_ID").longValue();
            PayslipSettingDto template = getTemplate(draftId, false, false);
            if (template == null) {
                return null;
            }
            template.getElements().forEach(el -> calculatedElements.add(templateElementToCalculatedElement(el)));
        }

        return calculatedElements;
    }

    private Map<String, Double> getMonthlyValues(Long contractId, LocalDate month, double holidayCounts) {

        Map<String, Double> monthlyValues = RequestContext.monthlyValues() != null && RequestContext.monthlyValues().containsKey(contractId) ?  RequestContext.monthlyValues().get(contractId)
                : hrDataRepository.findAllByContractIdAndMonth(contractId, month).stream().collect(Collectors.toMap(HRMonthlyData::getVariable, HRMonthlyData::getValue));
        if (monthlyValues.isEmpty()) {
            return Collections.emptyMap();
        }

        variableService.findAllUnsourcedVariables().forEach(variable -> {
            try {
                if (!monthlyValues.containsKey(variable.getKey())) {
                    monthlyValues.put(variable.getKey(), Double.valueOf(variable.getFormula()));
                }
            } catch (Exception e) {
                monthlyValues.put(variable.getKey(), 0.0);
            }

        });
        monthlyValues.put("HOLIDAYS", holidayCounts);
        return monthlyValues;
    }

    private void initPerRequestElements(PayslipSettingDto template, Long contractId) {
         perRequestElements().setCalculatedElements(new TreeSet<>((o1, o2) -> Integer.compare(indexOf(o1), indexOf(o2))));
         perRequestElements().setSettingsElements(template.getElements());
         perRequestElements().setShowIntermediate(template.isShowIntermediate());
         perRequestElements().setShowZero(template.isShowZero());
         perRequestElements().setEmployeeId(template.getEmployee().getId());
         perRequestElements().setCurrentDate(template.getPaymentDate());
        List<String> inFunctionsCodes = getInFunctionCodes();
        assert inFunctionsCodes != null;
        if (!inFunctionsCodes.isEmpty()) {
          //   perRequestElements().setFunctionElements(payslipElementSettingRepository.findAllByEmployeeIdAndClosed(contractId, inFunctionsCodes));
        }
    }

    private boolean isTemp(PayslipController.PayslipCalculatePayload payload )  {
        if (payload.getTempTemplate() == null) {
            return false;
        }
        if (payload.getTempTemplate().getId() != null) {
            return true;
        }
        return payload.getTempTemplate().getElements() != null && !payload.getTempTemplate().getElements().isEmpty();
    }

    public PayslipController.CalculatedPayslip calculate(PayslipController.PayslipCalculatePayload payload, Employee employee, boolean saveAsDraft, boolean clearContext) {

        try {
            LocalDate month = Objects.requireNonNullElse(payload.getMonth(), LocalDate.now()).withDayOfMonth(1);
            Map<String, Double> monthlyValues = getMonthlyValues(payload.getContractId(), month, payload.getHolidaysCount());
            Long templateId = monthlyValues.getOrDefault("TEMPLATE", -1d).longValue();
            if (payload.getTempTemplate() != null && payload.getTempTemplate().getId() != null) {
                templateId = payload.getTempTemplate().getId();
            }
            PayslipSettingDto template = templateId != -1 ? getTemplate(templateId, false, false) : getTemplate(templateId, false, true);
            if (template == null) {
                return null;
            }
            LinkedHashSet<PayslipElementDto> calculatedElements = loadDraftedIfPresent(payload, monthlyValues , template);
            initDictionaryService();
            Period period = DateUtils.getPayslipPeriod(payload.getMonth());
            dictionaryService().clearAllVariables();
            dictionaryService().setVariables(employee, template, monthlyValues, period);
            if (calculatedElements != null) {
                return new PayslipController.CalculatedPayslip(template, calculatedElements);
            }
            if (employee == null) {
                employee = employeeRepository.findById(payload.getResourceId()).orElse(null);
            }
            if (employee == null) {
                return null;
            }
            initRequestContext();
            template = mergeTempTemplate(template, payload.getTempTemplate());
            engine.setBindings(new SimpleBindings(dictionaryService().getAllVariables()), ScriptContext.GLOBAL_SCOPE);
            dictionaryService().setAddElements(true);
            initPerRequestElements(template, payload.getContractId());
            PayslipElementDto baseSalaryElement = calculateBaseSalary(template.getElements());

            double baseSalary = 0d;
            if (baseSalaryElement != null) {
                baseSalary = baseSalaryElement.getGain().doubleValue();
            }
            dictionaryService().setVariable(BASE_SALARY, baseSalary);
            for (PayslipSettingElementDto elementDto : template.getElements()) {
                calculateSection(elementDto);
            }
            if (saveAsDraft && !isTemp(payload)) {
                payslipDraftService.saveAsDraftAsync(SecurityUtils.getCurrentCompanyId(), template, perRequestElements().getCalculatedElements(), payload.getContractId(), payload.getResourceId(), employee, month);
            }
            return new PayslipController.CalculatedPayslip(template,  perRequestElements().getCalculatedElements());
        } finally {
            if (clearContext) {
                RequestContext.clear();
            }
        }

    }

    private int indexOf(PayslipElementDto elementDto) {
        EMPTY_SETTING_ELEMENT.setSection(elementDto.getCode());
        return  perRequestElements().getSettingsElements().indexOf(EMPTY_SETTING_ELEMENT);
    }

    private PayslipElementDto calculateBaseSalary(List<PayslipSettingElementDto> elements) {
        for (PayslipSettingElementDto element : elements) {
            SectionDto section = companySections().findByCode(element.getSection()).orElse(null);
            if (section != null && section.isSalary()) {
                return calculateSection(element);
            }
        }
        String code = defaultSectionSettings().findByKey(BASE_SALARY).map(SectionSetting::getCode).orElse(null);
        if (code != null) {
            return  companySections().findByCode(code)
                    .map(section -> calculateSection(code, section.getLabel(), null, null, null))
                    .orElse(null);
        }
        return null;
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
                    if ( companySections().getSections().containsKey(param)) {
                        codes.add(param);
                    }
                }
            }
        }

    }


    private List<String> getInFunctionCodes() {
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
        for (PayslipSettingElementDto element :  perRequestElements().getSettingsElements()) {
            SectionDto section =  companySections().findByCode(element.getSection()).orElse(null);
            if (section == null) {
                continue;
            }
            boolean isInCategory = prefix.equals(ATTRIBUTE_PREFIX) ? section.getLabelKeys() != null && section.getLabelKeys().contains(label) :
                    Objects.equals(label, SECTION_NATURE_ABBR.get(section.getNature()));
            if (!isInCategory) {
                continue;
            }
            PayslipElementDto el = calculateSection(element);
            if (el.isTotal() || section.isSalary()) {
                continue;
            }
            updateTotal(prefix, label, el.getGain(), el.getDeduction());
        }
        updateTotal(prefix, label, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private void updateTotal(String prefix, String label, BigDecimal gain, BigDecimal deduction) {
        String key = String.format(prefix + "%s", label);
        BigDecimal recentTotal = BigDecimal.valueOf( dictionaryService().findDoubleValue(key));
        dictionaryService().setVariable(key, recentTotal.add((gain != null ? gain : BigDecimal.ZERO)).subtract((deduction != null ? deduction : BigDecimal.ZERO)));
    }

    private PayslipElementDto getTotalElement(String label) {
        Double gain =  perRequestElements().getCalculatedElements().stream()
                .filter(element -> !element.isTotal())
                .map(PayslipElementDto::getGain)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .reduce(0d, Double::sum);
        Double deduction =  perRequestElements().getCalculatedElements().stream()
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
            if ( dictionaryService().getAddElements()) {
                PayslipElementDto alreadyExistsElement =  perRequestElements().getCalculatedElements().stream().filter(e -> Objects.equals(e.getCode(), element.getSection())).findFirst().orElse(null);
                if (alreadyExistsElement != null) {
                    return alreadyExistsElement;
                }
            }
            return calculateSection(element.getSection(), element.getLabel(), element.getBaseValue(), element.getRateValue(), element.getValue());
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
        return null;
    }

    private boolean isHidden(PayslipElementDto element) {
        if ((element.isTotal() && ! perRequestElements().isShowIntermediate()) ||
                (! perRequestElements().isShowZero() && element.isZero())) {
            return true;
        }
        return false;
    }

    public void addCalculatedElement(PayslipElementDto element) {
        EMPTY_SETTING_ELEMENT.setSection(element.getCode());
        if (! perRequestElements().getSettingsElements().contains(EMPTY_SETTING_ELEMENT)) {
            return;
        }
        if (!isHidden(element)) {
             perRequestElements().getCalculatedElements().add(element);
        }
    }

    private PayslipElementDto evaluateSectionFormulas(SectionDto section, String label, Double baseValue, Double rateValue, Double value) {
        AtomicReference<PayslipElementDto> elementDto = new AtomicReference<>(new PayslipElementDto());
        elementDto.get().setCode(section.getCode());
        elementDto.get().setLabel(label == null ? section.getLabel() : label);
        elementDto.get().setDescription(section.getDescription());
        elementDto.get().setTotal(section.isIntermediate());
        elementDto.get().setRatePercent(section.isRatePercent());
        Bindings context = new SimpleBindings();
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
                    if ( dictionaryService().getAddElements()) {
                        addCalculatedElement(elementDto.get());
                    }
                    return;
                }
                boolean hasOwnVariables = section.getVariables() != null && !section.getVariables().isEmpty();
                if (hasOwnVariables) {

                    LinkedHashMap<String, Object> currentVariables = new LinkedHashMap<>( dictionaryService().getAllVariables()); // first we need to save the current variables state
                    boolean currentAddElement = dictionaryService().getAddElements();
                    LinkedHashMap<String, String> currentVariableValues = new LinkedHashMap<>(10);
                    section.getVariables().forEach(sectionVariable -> currentVariableValues.put(sectionVariable.getVariableKey(), String.valueOf( dictionaryService().getInitialVariables().getOrDefault(sectionVariable.getVariableKey(), "0"))));
                    dictionaryService().setAddElements(false);
                    dictionaryService().getAllVariables().clear();
                    section.getVariables().forEach(sectionVariable -> dictionaryService().getInitialVariables().put(sectionVariable.getVariableKey(), Double.valueOf(sectionVariable.getValue())));
                    dictionaryService().getAllVariables().putAll( dictionaryService().getInitialVariables());
                    elementDto.set(evaluateSectionFormulas(section, label, baseValue, rateValue, value));
                    Double calculatedValue = dictionaryService().findDoubleValue(section.getCode());
                    //reset variables state
                    dictionaryService().getAllVariables().clear();
                    section.getVariables().forEach(sectionVariable -> dictionaryService().getInitialVariables().put(sectionVariable.getVariableKey(), currentVariableValues.getOrDefault(sectionVariable.getVariableKey(), "0")));
                    dictionaryService().getAllVariables().putAll(currentVariables);
                    dictionaryService().getAllVariables().putAll( dictionaryService().getInitialVariables());
                    dictionaryService().setVariable(section.getCode(), Objects.requireNonNullElse(calculatedValue, 0.0));
                    dictionaryService().setAddElements(currentAddElement);
                } else {
                    elementDto.set(evaluateSectionFormulas(section, label, baseValue, rateValue, value));
                }
                if ( dictionaryService().getAddElements()) {
                    addCalculatedElement(elementDto.get());
                }
            });
        }

        return elementDto.get();
    }

    private LocalDate getFunctionStartDate(String functionName, String param) {
        if (functionName.contains("FROM")) {
            Integer n = Integer.valueOf(param);
            if (n >=  perRequestElements().getCurrentDate().getMonthValue()) {
                return  perRequestElements().getCurrentDate().withMonth(n).withYear( perRequestElements().getCurrentDate().getYear() - 1).withDayOfMonth(1);
            } else {
                return  perRequestElements().getCurrentDate().withMonth(n).withDayOfMonth(1);
            }
        } else if (functionName.contains("BK")) {
            Integer n = Integer.valueOf(param);
            return  perRequestElements().getCurrentDate().minusMonths(n).withDayOfMonth(1);
        } else {
            return LocalDate.parse(param);
        }
    }

    private Double calculateFunctionByName(String functionName, String[] paramList) {
        // todo remove 'when' function if it is not needed
        if (Objects.equals(functionName, "WHEN")) {
            String section = paramList[0];
            String variableName = paramList[1];
            String value = paramList[2];
            if (Objects.nonNull(variableName) && Objects.nonNull(value) && Objects.nonNull(section)) {
                // here we recalculate the function by setting the variableName value to 'value'
                LinkedHashMap<String, Object> currentVariables = new LinkedHashMap<>( dictionaryService().getAllVariables()); // first we need to save the current variables state
                boolean currentAddElement = dictionaryService().getAddElements();
                String currentVariableValue = String.valueOf( dictionaryService().getInitialVariables().getOrDefault(variableName, "0"));
                dictionaryService().setAddElements(false);
                dictionaryService().getAllVariables().clear();
                dictionaryService().getInitialVariables().put(variableName, Double.valueOf(value));
                dictionaryService().getAllVariables().putAll( dictionaryService().getInitialVariables());
                calculateSection(section, null, null, null, null);
                Double result = dictionaryService().findDoubleValue(section);
                // now we need to restore the previous variables state
                dictionaryService().getAllVariables().clear();
                dictionaryService().getInitialVariables().put(variableName, currentVariableValue);
                dictionaryService().getAllVariables().putAll(currentVariables);
                dictionaryService().getAllVariables().putAll( dictionaryService().getInitialVariables());
                dictionaryService().setAddElements(currentAddElement);
                return result;

            }
            return 0.0;

        }
        if (Objects.equals(functionName, "EOFMONTH")) {
            return  perRequestElements().getCurrentDate().isEqual(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())) ? 1.0 : 0.0;
        } else {
            LocalDate startDate = getFunctionStartDate(functionName, paramList[1]);
            LocalDate endDate = functionName.equals("AVRG") ? LocalDate.parse(paramList[2]) :  perRequestElements().getCurrentDate();
            if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
                return 0.0;
            }
            DoubleStream values =  perRequestElements().getFunctionElements()
                    .stream().filter(el -> el.getSection().getCode().equals(paramList[0]) &&
                            el.getPayslipSetting().getPaymentDate().isBefore(endDate) &&
                            !el.getPayslipSetting().getPaymentDate().isBefore(startDate.withDayOfMonth(1)) &&
                             perRequestElements().getEmployeeId().equals(el.getPayslipSetting().getEmployee()))
                    .mapToDouble(PayslipSettingElement::getValue);
            if (functionName.contains("AVRG")) {
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
            if (! dictionaryService().hasVariable(code)) {
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
                    formula = formula.replace(matcher.group(), String.valueOf( dictionaryService().findDoubleValue(matcher.group())));
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

    public PayslipDetailDto getPayslipDetails(PayslipController.PayslipCalculatePayload payload) {
        if (payslipSettingRepository.isClosedPayslip(payload.getMonth().withDayOfMonth(1), payload.getContractId())) {
            Long templateId = hrDataRepository.findHrMonthlyDataByVariableAndContractIdAndMonth("TEMPLATE", payload.getContractId(), payload.getMonth().withDayOfMonth(1)).map(HRMonthlyData::getValue).orElse(-1d).longValue();
            PayslipSettingDto template = templateId != -1 ? getTemplate(templateId, false, false) : getTemplate(templateId, false, true);
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

    private PayslipSettingDto getTemplate(Long id, boolean draft, boolean isDefault) {
        if (!draft) {
            if(isDefault) {
                return RequestContext.defaultTemplate() != null ? RequestContext.defaultTemplate() : payslipSettingService.findDefaultTemplate().orElse(null);
            } else {
                return RequestContext.templates() != null ? RequestContext.templates().get(id) : payslipSettingService.findById(id).orElse(null);
            }
        } else {
            return RequestContext.templates() != null ? RequestContext.templates().get(id) : payslipSettingService.findDraftById(id).orElse(null);
        }
    }

}
