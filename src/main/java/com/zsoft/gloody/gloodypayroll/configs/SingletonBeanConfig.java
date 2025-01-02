package com.zsoft.gloody.gloodypayroll.configs;

import com.zsoft.gloody.gloodypayroll.models.beans.PerRequestElements;
import com.zsoft.gloody.gloodypayroll.services.SectionDictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class SingletonBeanConfig {

    @Bean
    @RequestScope
    public SectionDictionaryService dictionaryService() {
        return new SectionDictionaryService();
    }
    @Bean
    @RequestScope
    public PerRequestElements PerRequestElements() {
        return new PerRequestElements();
    }


}
