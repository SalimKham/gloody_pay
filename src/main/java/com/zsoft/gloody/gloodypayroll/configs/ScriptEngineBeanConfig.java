package com.zsoft.gloody.gloodypayroll.configs;

import com.zsoft.gloody.gloodypayroll.services.IrgCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.LocalDate;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.IRG_YEAR;
import static com.zsoft.gloody.gloodypayroll.configs.Constants.IS_HANDICAP;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class ScriptEngineBeanConfig {


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
            engine.eval("function IRG(b) {" +
                    "   return irgCalculator.calculateIrg(b," + IRG_YEAR +  "," + IS_HANDICAP +");" +
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
