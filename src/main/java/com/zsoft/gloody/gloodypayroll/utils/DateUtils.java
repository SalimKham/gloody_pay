package com.zsoft.gloody.gloodypayroll.utils;

import com.zsoft.gloody.gloodypayroll.models.entities.Period;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

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

    public static List<LocalDate> getDaysBetween(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate.isBefore(endDate) ? startDate : endDate;
        LocalDate end = endDate.isBefore(startDate) ? startDate : endDate;
        long days = start.until(end, ChronoUnit.DAYS);
        return LongStream.rangeClosed(0, days)
                .mapToObj(start::plusDays)
                .collect(Collectors.toList());
    }

    public static List<LocalDate> getWorkingDays(LocalDate start, LocalDate end) {
        return DateUtils.getDaysBetween(start, end)
                .stream()
                .filter(d -> !DateUtils.isWeekend(d) && !DateUtils.isHoliday(d))
                .collect(Collectors.toList());
    }

    public static boolean isInPeriod(LocalDate date, Period period) {
        return (date.isEqual(period.start()) || date.isAfter(period.start()))
                && (date.isEqual(period.end()) || date.isBefore(period.end()));
    }

    public static boolean isWeekend(LocalDate date) {
        Set<DayOfWeek> weekend = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
        return weekend.contains(date.getDayOfWeek());
    }

    public static boolean isHoliday(LocalDate date) {
        Set<LocalDate> holidays = Set.of(
                LocalDate.of(2023, Month.JULY, 5),
                LocalDate.of(2023, Month.NOVEMBER, 1)
        );
        return holidays.contains(date);
    }
}
