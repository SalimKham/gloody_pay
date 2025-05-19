package com.zsoft.gloody.gloodypayroll.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class LogUtility {
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    public static String successMsg(String message, Object... var2) {
        return (GREEN + String.format(message, var2)+ RESET);
    }
}
