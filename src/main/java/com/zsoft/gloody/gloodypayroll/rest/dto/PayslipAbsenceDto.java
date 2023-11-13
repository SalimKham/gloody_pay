package com.zsoft.gloody.gloodypayroll.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.enums.AbsenceType;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PayslipAbsenceDto {
    @JsonProperty(value = "start")
    private LocalDate startDate;
    @JsonProperty(value = "end")
    private LocalDate endDate;
    @JsonProperty(value = "type")
    @Builder.Default
    private AbsenceType type = AbsenceType.AB;
    @JsonProperty(value = "start_half_day")
    @Builder.Default
    private boolean startHalfDay = false;
    @JsonProperty(value = "end_half_day")
    @Builder.Default
    private boolean endHalfDay = false;

    public boolean isCP() {
        return Objects.equals(type, AbsenceType.CP);
    }

    public boolean isAbsence() {
        return Objects.equals(type, AbsenceType.AB);
    }

    public Period getPeriod() {
        return new Period(startDate, endDate);
    }

    public double count(List<LocalDate> workingDays) {
        if (startDate == null || endDate == null) return 0;
        Set<LocalDate> workingDates = new HashSet<>(workingDays);
        return DateUtils.getDaysBetween(startDate, endDate)
                .stream()
                .filter(date -> !isAbsence() || workingDates.contains(date))
                .map(date -> {
                    if (isStartHalfDay() && startDate.equals(date)) {
                        return 0.5;
                    } else if (isEndHalfDay() && endDate.equals(date)) {
                        return 0.5;
                    }
                    return 1d;
                })
                .mapToDouble(Double::valueOf)
                .sum();
    }
}
