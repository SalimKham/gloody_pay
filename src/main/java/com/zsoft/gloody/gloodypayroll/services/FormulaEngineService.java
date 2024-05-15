package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.script.*;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.*;

@AllArgsConstructor
@Service
@Slf4j
public class FormulaEngineService {


    public ScriptEngine getInstance(Map<String, Object> context) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        initDefaultFunctions(engine);
        context.forEach(engine::put);
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

    public boolean isInvalid(String formula) {
        if (formula == null) {
            return false;
        }
        List<String> variables = Arrays.stream(Constants.class.getDeclaredFields())
                .filter(field -> field.getType().equals(String.class))
                .filter(field -> Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
                .map(field -> ReflectionUtils.getField(field, null))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        String variablesRegex = String.join("|", variables);
        String functionsRegex = "MIN|MAX|FLOOR|CEIL|IRG";
        String regex = Arrays.stream(new String[]{"[\\s\\t\\n\\r<>=?:+\\-*/,;()0-9]", "[A-Z]\\d{3}", variablesRegex, functionsRegex})
                .filter(str -> str != null && !str.isEmpty())
                .map(Object::toString)
                .collect(Collectors.joining("|"));
        Pattern pattern = Pattern.compile("(" + regex + ")+", Pattern.MULTILINE);
        Matcher m = pattern.matcher(formula);
        if (!m.matches()) {
            return true;
        }
        Map<String, Object> context = variables.stream().collect(Collectors.toMap(s -> s, s -> 1));
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        context.forEach(engine::put);
        try {
            ((Compilable) engine).compile(formula);
            return false;
        } catch (ScriptException e) {
            return true;
        }
    }
}
