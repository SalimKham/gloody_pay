package com.zsoft.gloody.gloodypayroll.configs;


import com.zsoft.gloody.gloodypayroll.models.beans.CompanySections;
import com.zsoft.gloody.gloodypayroll.models.beans.DefaultSectionSettings;
import com.zsoft.gloody.gloodypayroll.models.beans.PerRequestElements;
import com.zsoft.gloody.gloodypayroll.models.repositories.HRDataRepository;
import com.zsoft.gloody.gloodypayroll.models.repositories.PayslipElementSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipFunctionElement;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.services.PayslipSettingReadOnlyService;
import com.zsoft.gloody.gloodypayroll.services.SectionDictionaryService;
import com.zsoft.gloody.gloodypayroll.services.SectionService;
import jakarta.persistence.Tuple;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RequestContext {
    private static final ThreadLocal<SectionDictionaryService> dictionaryService = new InheritableThreadLocal<>();
    private static final ThreadLocal<PerRequestElements> perRequestElements = new InheritableThreadLocal<>();
    private static final ThreadLocal<CompanySections> companySections = new InheritableThreadLocal<>();
    private static final ThreadLocal<DefaultSectionSettings> defaultSectionSettings = new InheritableThreadLocal<>();
    private static final ThreadLocal<Map<Long, PayslipSettingDto>> templates = new InheritableThreadLocal<>();
    private static final ThreadLocal<PayslipSettingDto> defaultTemplate = new InheritableThreadLocal<>();
    private static final ThreadLocal<Map<Long, Map<String, Double>>> monthlyValues = new InheritableThreadLocal<>();
    private static final ThreadLocal<Map<Long, List<PayslipFunctionElement>>> functionElements = new InheritableThreadLocal<>();

    public static void setDictionaryService(SectionDictionaryService ds) {
      dictionaryService.set(ds);
    }

    public static void initRequestContext() {
        initCompanySections();
        initDefaultSectionSettings();
        initPerRequestElements();
    }

    public static void initDictionaryService() {
        setDictionaryService(new SectionDictionaryService());
    }

    public static SectionDictionaryService dictionaryService() {
        if (dictionaryService.get() == null) {
            initDictionaryService();
        }
        return dictionaryService.get();
    }

    public static void setPerRequestElements(PerRequestElements pE) {
        perRequestElements.set(pE);
    }

    public static void initPerRequestElements() {
        setPerRequestElements(new PerRequestElements());
    }

    public static PerRequestElements perRequestElements() {
        if (perRequestElements.get() == null) {
            initPerRequestElements();
        }
        return perRequestElements.get();
    }

    public static void setTemplates(Map<Long, PayslipSettingDto> tps) {
        templates.set(tps);
    }

    public static void initTemplates() {
        setTemplates(SpringContextHolder.getBean(PayslipSettingReadOnlyService.class).loadPayslipSettingsMap());
    }

    public static Map<Long, PayslipSettingDto> templates() {
        return templates.get();
    }

    public static void setDefaultTemplate(PayslipSettingDto deft) {
        defaultTemplate.set(deft);
    }

    public static void initDefault() {
        setDefaultTemplate(SpringContextHolder.getBean(PayslipSettingReadOnlyService.class).findDefaultTemplate().orElse(null));
    }

    public static PayslipSettingDto defaultTemplate() {
        return defaultTemplate.get();
    }


    public static void setCompanySections(CompanySections cS) {
        companySections.set(cS);
    }

    public static void initCompanySections() {
        setCompanySections(new CompanySections(SpringContextHolder.getBean(SectionService.class).findAllAsMap()));
    }

    public static CompanySections companySections() {
        if (companySections.get() == null) {
            initCompanySections();
        }
        return companySections.get();
    }


    public static void setDefaultSectionSettings(DefaultSectionSettings dS) {
        defaultSectionSettings.set(dS);
    }

    public static void initDefaultSectionSettings() {
        setDefaultSectionSettings(SpringContextHolder.getBean(SectionService.class).findDefaultSections());
    }

    public static DefaultSectionSettings defaultSectionSettings() {
        if (defaultSectionSettings.get() == null) {
            initDefaultSectionSettings();
        }
        return defaultSectionSettings.get();
    }

    public static void setMonthlyValues(Map<Long, Map<String, Double>> mv) {
        monthlyValues.set(mv);
    }



    public static void initMonthlyValues(Set<Long> resourceIds) {
        List<Tuple> tuples = SpringContextHolder.getBean(HRDataRepository.class).getMonthlyValues(LocalDate.now().withDayOfMonth(1), resourceIds);
        Map<Long, Map<String, Double>> result = tuples.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(0, Long.class),
                        Collectors.toMap(
                                tuple -> tuple.get(1, String.class),
                                tuple -> tuple.get(2, Double.class),
                                (existing, replacement) -> existing
                        )
                ));
        setMonthlyValues(result);
    }

    public static Map<Long, Map<String, Double>> monthlyValues() {
        return monthlyValues.get();
    }


    public static void setFunctionElements(Map<Long, List<PayslipFunctionElement>> fe) {
        functionElements.set(fe);
    }



    public static void initFunctionElements(Set<Long> resourceIds, List<String> codes) {
        List<PayslipFunctionElement> elements = SpringContextHolder.getBean(PayslipElementSettingRepository.class).findAllByEmployeeIdAndClosed(resourceIds, codes);
        Map<Long, List<PayslipFunctionElement>> result = elements.stream().collect(Collectors.groupingBy(PayslipFunctionElement::resourceId));
        setFunctionElements(result);
    }

    public static Map<Long, List<PayslipFunctionElement>> functionElements() {
        return functionElements.get();
    }


    public static void clear() {
        dictionaryService.remove();
        defaultSectionSettings.remove();
        perRequestElements.remove();
        defaultTemplate.remove();
        monthlyValues.remove();
        templates.remove();
        companySections.remove();
        functionElements.remove();
    }
}
