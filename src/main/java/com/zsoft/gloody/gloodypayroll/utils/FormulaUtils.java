package com.zsoft.gloody.gloodypayroll.utils;

import com.zsoft.gloody.gloodypayroll.services.IrgCalculatorService;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.zsoft.gloody.gloodypayroll.configs.RequestContext.dictionaryService;

public class FormulaUtils {
    public static final String FUNCTION_REGEX = "\\b(\\w+)\\s*\\(\\s*([^)]*)\\s*\\)";
    public static double IRG(Double value) {
       return IrgCalculatorService.IRG(value);
    }
    public static String parseFormula(String formula, String term) {
        if (formula.contains("?")) {
            String regex = "(?<![<>==])\\b" + Pattern.quote(term) + "\\b";
            String replacement = term + " != 0";
            String condition = formula.substring(0, formula.indexOf("?"));
            String newCondition = condition.replaceAll(regex,   replacement);
            formula = formula.replace(condition, newCondition);
        }
        if (dictionaryService().getAllVariables().containsKey(term) || Objects.equals(term, "BASE") || Objects.equals(term, "RATE") ) {
            if (!formula.contains("#" + term) && !term.matches(FUNCTION_REGEX)) {
                formula = formula.replaceAll("\\b" + term + "\\b", "#" + term);
            }
        }

        return formula;
    }
    public static String parseIrgFunction(String formula) {
        String parsedFormula = formula.replaceAll("\\(BASE\\)", "(#BASE)" );
        return parsedFormula.replaceAll("IRG\\(", "T(com.zsoft.gloody.gloodypayroll.utils.FormulaUtils).IRG(" );
    }
}
