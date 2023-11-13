package com.zsoft.gloody.gloodypayroll.configs;

import com.zsoft.gloody.gloodypayroll.models.repositories.VariableSettingRepository;
import com.zsoft.gloody.gloodypayroll.services.FormulaEngineService;
import com.zsoft.gloody.gloodypayroll.services.SectionDictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@RequiredArgsConstructor
@Configuration
public class SingletonBeanConfig {
    private final VariableSettingRepository settingRepository;
    private final FormulaEngineService engineService;

    @Bean
    @RequestScope
    public SectionDictionaryService dictionaryService() {
        return new SectionDictionaryService(settingRepository, engineService);
    }
}
