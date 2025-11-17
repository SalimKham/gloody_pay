package com.zsoft.gloody.gloodypayroll.utils;

import com.zsoft.gloody.gloodypayroll.services.IrgCalculatorService;

public class FormulaUtils {
    public static double IRG(Double value) {
       return IrgCalculatorService.IRG(value);
    }
}
