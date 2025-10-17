package com.zsoft.gloody.gloodypayroll.utils;

import com.zsoft.gloody.gloodypayroll.models.entities.Period;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

public class DateUtils {
    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static LocalDate startOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate endOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    public static Period getPayslipPeriod(LocalDate paymentMonth) {
        LocalDate month = Optional.ofNullable(paymentMonth).orElse(LocalDate.now());
        return new Period(DateUtils.startOfMonth(month), DateUtils.endOfMonth(month));
    }

    public static boolean  isDateBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isAfter(endDate) && !date.isBefore(startDate);
    }

}
