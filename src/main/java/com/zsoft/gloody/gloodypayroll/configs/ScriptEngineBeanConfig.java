package com.zsoft.gloody.gloodypayroll.configs;

import com.zsoft.gloody.gloodypayroll.models.beans.CompanySections;
import com.zsoft.gloody.gloodypayroll.models.beans.DefaultSectionSettings;
import com.zsoft.gloody.gloodypayroll.models.entities.SectionSetting;
import com.zsoft.gloody.gloodypayroll.models.repositories.SectionSettingRepository;
import com.zsoft.gloody.gloodypayroll.services.IrgCalculatorService;
import com.zsoft.gloody.gloodypayroll.services.SectionService;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.IRG_YEAR;
import static com.zsoft.gloody.gloodypayroll.configs.Constants.IS_HANDICAP;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class ScriptEngineBeanConfig {

    private final SectionService sectionService;
    private final SectionSettingRepository sectionSettingRepository;


    @Bean
    @RequestScope
    public CompanySections companySections() {
        return new  CompanySections(sectionService.findAllAsMap());
    }

    @Bean
    @RequestScope
    public DefaultSectionSettings defaultSectionSettings() {
        return new DefaultSectionSettings(sectionSettingRepository.findAllByCompany(SecurityUtils.getCurrentCompanyId()).stream().collect(Collectors.toMap(SectionSetting::getKey, sectionSetting -> sectionSetting)));
    }

    @Bean
    public ScriptEngine scriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        initDefaultFunctions(engine);
        return engine;
    }

    private void initDefaultFunctions(ScriptEngine engine) {
        try {
            engine.put("irgCalculator", new IrgCalculatorService());
            engine.put(IS_HANDICAP, false);
            engine.put(IRG_YEAR, LocalDate.now().getYear());
            engine.eval("function IRG(b, y, h) {" +
                    "   if(b == undefined){b = 0;} " +
                    "   if(y == undefined){y = " + IRG_YEAR + ";} " +
                    "   if(h == undefined){h = " + IS_HANDICAP + ";} " +
                    "   return irgCalculator.calculateIrg(b, y, h);" +
                    "}");
            engine.eval("var max = MAX = Math.max;");
            engine.eval("var min = MIN = Math.min;");
            engine.eval("var ceil = CEIL = Math.ceil;");
            engine.eval("var floor = FLOOR = Math.floor;");
        } catch (ScriptException e) {
            log.error(e.getMessage());
        }
    }
}
