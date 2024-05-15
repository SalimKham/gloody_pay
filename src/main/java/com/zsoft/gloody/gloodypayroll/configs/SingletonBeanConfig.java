package com.zsoft.gloody.gloodypayroll.configs;

import com.zsoft.gloody.gloodypayroll.models.repositories.VariableSettingRepository;
import com.zsoft.gloody.gloodypayroll.services.SectionDictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.script.ScriptEngine;


@RequiredArgsConstructor
@Configuration
@Slf4j
public class SingletonBeanConfig {
    private final VariableSettingRepository settingRepository;
    private final ScriptEngine engine;

    @Bean
    @RequestScope
    public SectionDictionaryService dictionaryService() {
        return new SectionDictionaryService(settingRepository, engine);
    }
}
