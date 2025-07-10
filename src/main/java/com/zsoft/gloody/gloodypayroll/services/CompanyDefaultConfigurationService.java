package com.zsoft.gloody.gloodypayroll.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zsoft.gloody.gloodypayroll.models.entities.*;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionType;
import com.zsoft.gloody.gloodypayroll.models.repositories.*;
import com.zsoft.gloody.gloodypayroll.support.StringListConverter;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyDefaultConfigurationService {
    private final PayslipConfigRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final PayslipSettingRepository payslipSettingRepository;
    private final VariableSettingRepository variableSettingRepository;
    private final LabelSettingRepository labelSettingRepository;
    private  StringListConverter stringListConverter;

    @Transactional
    public void initCompanyConfiguration(Long companyId) {
        JsonNode data  = getDefaultData();
        if (data == null) {
            return;
        }
        stringListConverter = new StringListConverter();
        initDefaultVariables(companyId, data.get("default-variables"));
        initDefaultCategories(companyId, data.get("default-categories"));
        initDefaultLabels(companyId, data.get("default-labels"));
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
            if (variableSettingRepository.existsByKeyAndCompany(obj.get("key").asText(), companyId)) {
                continue;
            }
            String source  = obj.has("source") ? obj.get("source").asText() : null;
            Long id = null;
            if(source != null) {
                 id = variableSettingRepository.findTopBySourceAndCompany(source, companyId).map(VariableSetting::getId).orElse(null);
            }
            VariableSetting variableSetting = VariableSetting.builder()
                    .id(id)
                    .key(obj.get("key").asText())
                    .formula(obj.get("formula").asText())
                    .label(obj.has("label") ? obj.get("label").asText() : null)
                    .source(obj.has("source") ? obj.get("source").asText() : null)
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

    private void initDefaultLabels(Long companyId, JsonNode data) {
        if (data == null) {
            return;
        }
        List<LabelSetting> labelSettings = new ArrayList<>();
        Map<String, String> parentMap = new HashMap<>();
        for (JsonNode obj : data) {
            if (!obj.isObject() || !obj.has("key") || !obj.has("label")) {
                continue;
            }
            if (labelSettingRepository.existsByKeyAndCompany(obj.get("key").asText(), companyId)) {
                continue;
            }
            LabelSetting labelSetting = LabelSetting.builder()
                    .key(obj.get("key").asText())
                    .label(obj.get("label").asText())
                    .updatedAt(LocalDateTime.now())
                    .company(companyId)
                    .build();
            if (obj.has("parent_id")) {
                parentMap.put(obj.get("key").asText(), obj.get("parent_id").asText());
            }
            labelSettings.add(labelSetting);
        }
        if (!labelSettings.isEmpty()) {
            labelSettingRepository.saveAll(labelSettings);
            if (!parentMap.isEmpty()) {
                parentMap.forEach((labelKey, parentKey) -> labelSettingRepository.updateParentId(labelKey, parentKey, companyId) );
            }
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
            if (categoryRepository.existsByCodeAndCompany(obj.get("code").asText(), companyId)) {
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
        Integer identifier = sectionRepository.findTopByCompanyOrderByIdentifierDesc(SecurityUtils.getCurrentCompanyId()).map(section -> section.getIdentifier() + 1).orElse(9001);
        for (JsonNode obj : data) {
            if (!obj.isObject() || !obj.has("code") || !obj.has("label" ) || !obj.has("category") || !obj.has("type")) {
                continue;
            }
            if (sectionRepository.existsByCodeAndCompany(obj.get("code").asText(), companyId)) {
                continue;
            }
            Section section = Section.builder()
                    .label(obj.get("label").asText())
                    .code(obj.get("code").asText())
                    .identifier(identifier++)
                    .category(categoryRepository.findByCodeAndCompany(obj.get("category").asText(), companyId)
                            .map(sectionCategory -> SectionCategory.builder().id(sectionCategory.getId()).build()).orElse(null))
                    .type(SectionType.valueOf(obj.get("type").asText()))
                    .baseFormula(obj.has("base_formula") ? obj.get("base_formula").asText() : null)
                    .rateFormula(obj.has("rate_formula") ? obj.get("rate_formula").asText() : null)
                    .labelKeys(obj.has("label_keys") ? stringListConverter.convertToEntityAttribute(obj.get("label_keys").asText()) : null)
                    .valueFormula(obj.has("value_formula") ? obj.get("value_formula").asText() : null)
                    .description(obj.has("description") ? obj.get("description").asText() : null)
                    .ratePercent(obj.has("rate_percent") && obj.get("rate_percent").asBoolean())
                    .company(companyId)
                    .build();
            sections.add(section);
        }
        if (!sections.isEmpty()) {
            sectionRepository.saveAll(sections);
        }
    }

    private void initDefaultTemplates(Long companyId, JsonNode data) {
        if(payslipSettingRepository.existsByCompanyAndByDefaultIsTrue(companyId)) {
            return;
        }
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
                    .byDefault(obj.has("isDefault") && obj.get("isDefault").asBoolean())
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
