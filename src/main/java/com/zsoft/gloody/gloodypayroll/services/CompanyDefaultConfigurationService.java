package com.zsoft.gloody.gloodypayroll.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zsoft.gloody.gloodypayroll.models.entities.*;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionType;
import com.zsoft.gloody.gloodypayroll.models.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class CompanyDefaultConfigurationService {
    private final PayslipConfigRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final PayslipSettingRepository payslipSettingRepository;
    private final VariableSettingRepository variableSettingRepository;
    private final SectionSettingRepository sectionSettingRepository;
    //@Async
    @Transactional
    public void initCompanyConfiguration(Long companyId) {
        JsonNode data  = getDefaultData();
        if (data == null) {
            return;
        }
        initDefaultVariables(companyId, data.get("default-variables"));
        initDefaultSectionCodes(companyId, data.get("default_section_codes"));
        initDefaultCategories(companyId, data.get("default-categories"));
        initSections(companyId, data.get("default-sections"));
        initDefaultTemplates(companyId, data.get("default-templates"));
    }

    private void initDefaultVariables(Long companyId, JsonNode data) {
        if (data == null) {
            return;
        }
        List<VariableSetting> variables = new ArrayList<>();
        for (JsonNode obj : data) {
            if (!obj.isObject() || !obj.has("key") || !obj.has("formula")) {
                continue;
            }
            VariableSetting variableSetting = VariableSetting.builder()
                    .key(obj.get("key").asText())
                    .formula(obj.get("formula").asText())
                    .label(obj.has("label") ? obj.get("label").asText() : null)
                    .description(obj.has("description") ? obj.get("description").asText() : null)
                    .updatedAt(LocalDateTime.now())
                    .company(companyId)
                    .build();
            variables.add(variableSetting);
        }
        if (!variables.isEmpty()) {
            variableSettingRepository.saveAll(variables);
        }
    }

    private void initDefaultSectionCodes(Long companyId, JsonNode data) {
        if (data == null) {
            return;
        }
        List<SectionSetting> codes = new ArrayList<>();
        for (JsonNode obj : data) {
            if (!obj.isObject() || !obj.has("key") || !obj.has("code")) {
                continue;
            }
            SectionSetting sectionCode = SectionSetting.builder()
                    .key(obj.get("key").asText())
                    .code(obj.get("code").asText())
                    .company(companyId)
                    .build();
            codes.add(sectionCode);
        }
        if (!codes.isEmpty()) {
            sectionSettingRepository.saveAll(codes);
        }
    }


    private void initDefaultCategories(Long companyId, JsonNode data) {
        if (data == null) {
            return;
        }
        List<SectionCategory> categories = new ArrayList<>();
        for (JsonNode obj : data) {
            if (!obj.isObject() || !obj.has("name") || !obj.has("code")) {
                continue;
            }
            SectionCategory category = SectionCategory.builder()
                    .name(obj.get("name").asText())
                    .code(obj.get("code").asText())
                    .company(companyId)
                    .build();
            categories.add(category);
        }
        if (!categories.isEmpty()) {
            categoryRepository.saveAll(categories);
        }
    }

    private void initSections(Long companyId, JsonNode data) {
        if (data == null) {
            return;
        }
        List<Section> sections = new ArrayList<>();
        for (JsonNode obj : data) {
            if (!obj.isObject() || !obj.has("code") || !obj.has("label" ) || !obj.has("category") || !obj.has("type")) {
                continue;
            }
            Section section = Section.builder()
                    .label(obj.get("label").asText())
                    .code(obj.get("code").asText())
                    .category(categoryRepository.findByCodeAndCompany(obj.get("category").asText(), companyId)
                            .map(sectionCategory -> SectionCategory.builder().id(sectionCategory.getId()).build()).orElse(null))
                    .type(SectionType.valueOf(obj.get("type").asText()))
                    .baseFormula(obj.has("baseFormula") ? obj.get("baseFormula").asText() : null)
                    .rateFormula(obj.has("rateFormula") ? obj.get("rateFormula").asText() : null)
                    .valueFormula(obj.has("valueFormula") ? obj.get("valueFormula").asText() : null)
                    .description(obj.has("description") ? obj.get("description").asText() : null)
                    .company(companyId)
                    .build();
            sections.add(section);
        }
        if (!sections.isEmpty()) {
            sectionRepository.saveAll(sections);
        }
    }

    private void initDefaultTemplates(Long companyId, JsonNode data) {
        if (data == null) {
            return;
        }
        for (JsonNode obj : data) {
            if (!obj.isObject() || !obj.has("name")) {
                continue;
            }
            PayslipSetting payslipSetting = PayslipSetting.builder()
                    .name(obj.get("name").asText())
                    .template(true)
                    .showIntermediate(true)
                    .company(companyId)
                    .build();
            //adding elements for template
            if (obj.has("elements")) {
                List<PayslipSettingElement> elements = new ArrayList<>();
                int elementIndex = 0;
                for (JsonNode tObj : obj.get("elements")) {
                    if (!tObj.isObject() || !tObj.has("sectionCode")) {
                        continue;
                    }
                    PayslipSettingElement payslipSettingElement = PayslipSettingElement.builder()
                            .section(sectionRepository.findByCodeAndCompany(tObj.get("sectionCode").asText(), companyId)
                                    .map(s -> Section.builder().id(s.getId()).build())
                                    .orElse(null))
                            .elementOrder(elementIndex++)
                            .value(tObj.has("value") ? tObj.get("value").asDouble() : null)
                            .rateValue(tObj.has("rateValue") ? tObj.get("rateValue").asDouble() : null)
                            .baseValue(tObj.has("baseValue") ? tObj.get("baseValue").asDouble() : null)
                            .company(companyId)
                            .build();
                    elements.add(payslipSettingElement);
                }
                if (!elements.isEmpty()) {
                    payslipSetting.setElements(elements);
                }
            }

            payslipSettingRepository.save(payslipSetting);

        }

    }

    private JsonNode getDefaultData() {
        try {
            String path = "defaults-fr.json";
            File file = ResourceUtils.getFile("classpath:data/" + path);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(file);
        } catch (IOException ignored) {
        }
        return null;
    }
}
