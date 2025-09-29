package com.zsoft.gloody.gloodypayroll.configs;

import java.math.BigDecimal;

public class Constants {
    public static final String BASE_SALARY = "BASE_SALARY";
    public static final String SECTIONS_PREFIX = "R";
    public static final String IS_DAILY_RATE = "IS_DAILY_RATE";
    public static final String IS_HOURLY_RATE = "IS_HOURLY_RATE";
    public static final String DAYS_BY_WEEK = "DAYS_BY_WEEK";
    public static final String HOURS_BY_WEEK = "HOURS_BY_WEEK";
    public static final String DAYS_NBR = "DAYS_NBR";
    public static final String HANDICAP = "HANDICAP";
    public static final String WORKING_DAYS_NBR = "WORKING_DAYS_NBR";
    public static final String CONTRACT_OFF_DAYS = "CONTRACT_OFF_DAYS"; // if contract start in the middle of the month
    public static final String HOURS_NBR = "HOURS_NBR";
    public static final String WORKING_HOURS_NBR = "WORKING_HOURS_NBR";
    public static final String PAYMENT_START = "PAYMENT_START";
    public static final String PAYMENT_END = "PAYMENT_END";
    public static final String IRG_YEAR = "IRG_YEAR";
    public static final String IS_HANDICAP = "IS_HANDICAP";
    public static final String RATE = "RATE";
    public static final String BASE = "BASE";
    public static final BigDecimal ZERO = BigDecimal.ZERO;

    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}
