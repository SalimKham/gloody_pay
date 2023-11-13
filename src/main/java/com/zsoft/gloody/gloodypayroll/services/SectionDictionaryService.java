package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.enums.PayslipRate;
import com.zsoft.gloody.gloodypayroll.models.repositories.VariableSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipAbsenceDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.time.LocalDate;
import java.util.*;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.*;

@RequiredArgsConstructor
public class SectionDictionaryService extends TenantSupportService {
    private final VariableSettingRepository settingRepository;
    private final FormulaEngineService engineService;
    private LinkedHashMap<String, Object> variables;

    @PostConstruct
    private void initVariables() {
        variables = new LinkedHashMap<>();
        settingRepository.findAll().forEach(setting -> {
            try {
                ScriptEngine engine = engineService.getInstance();
                Object result = engine.eval(setting.getFormula());
                if (result != null) {
                    setVariable(setting.getKey(), valueOf(result));
                }
            } catch (ScriptException ignored) {
            }
        });
        setVariable(IRG_YEAR, LocalDate.now().getYear());
    }

    public Object findValue(String key) {
        if (variables.containsKey(key)) {
            return variables.get(key);
        }
        return null;
    }

    public Double findDoubleValue(String key) {
        if (variables.containsKey(key)) {
            return valueOf(variables.get(key));
        }
        return 0d;
    }

    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }

    public LinkedHashMap<String, Object> getAllVariables() {
        return variables;
    }

    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    public void setVariables(PayslipSettingDto dto, Period period, List<LocalDate> workingDays) {
        if (dto == null) {
            return;
        }
        setVariable(PAYMENT_START, DateUtils.startOfMonth(period.getStart()));
        setVariable(PAYMENT_END, DateUtils.endOfMonth(period.getEnd()));
        setVariable(IRG_YEAR, period.getStart().getYear());
        if (dto.getRate() != null) {
            setVariable(IS_DAILY_RATE, Objects.equals(dto.getRate(), PayslipRate.DAILY));
            setVariable(IS_HOURLY_RATE, Objects.equals(dto.getRate(), PayslipRate.HOURLY));
        }
        double countAbsenceDays = workingDays.stream()
                .map(d -> {
                    if (dto.getAbsences() == null) return 0d;
                    for (PayslipAbsenceDto ab : dto.getAbsences()) {
                        if (DateUtils.isInPeriod(d, ab.getPeriod())) {
                            if (d.isEqual(ab.getStartDate()) && ab.isStartHalfDay()) return 0.5d;
                            if (d.isEqual(ab.getEndDate()) && ab.isEndHalfDay()) return 0.5d;
                            return 1d;
                        }
                    }
                    return 0d;
                })
                .mapToDouble(Double::valueOf)
                .sum();
        double countWorkingDays = findDoubleValue(DAYS_NBR) - countAbsenceDays;
        double hoursByDay = findDoubleValue(HOURS_BY_WEEK) / findDoubleValue(DAYS_BY_WEEK);
        setVariable(WORKING_DAYS_NBR, countWorkingDays);
        setVariable(WORKING_HOURS_NBR, (countWorkingDays * hoursByDay) - findDoubleValue(ABSENCES));
        if ((boolean) findValue(IS_DAILY_RATE)) {
            setVariable(ABSENCES, countAbsences(dto.getAbsences(), workingDays));
        } else {
            setVariable(ABSENCES, countAbsences(dto.getAbsences(), workingDays) * hoursByDay);
        }
        setVariable(CP, countCP(dto.getAbsences(), workingDays));
        if (dto.getEmployee() != null) {
            setVariable(BASE_SALARY, dto.getEmployee().getBaseSalary());
        }
    }

    private Double valueOf(Object value) {
        if (value == null) return 0d;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0d;
        }
    }

    private double countAbsences(List<PayslipAbsenceDto> absences, List<LocalDate> workingDays) {
        return Optional.ofNullable(absences)
                .orElse(Collections.emptyList())
                .stream()
                .filter(PayslipAbsenceDto::isAbsence)
                .map(a -> a.count(workingDays))
                .mapToDouble(Double::valueOf)
                .sum();
    }

    private double countCP(List<PayslipAbsenceDto> absences, List<LocalDate> workingDays) {
        return Optional.ofNullable(absences)
                .orElse(Collections.emptyList())
                .stream()
                .filter(PayslipAbsenceDto::isCP)
                .map(a -> a.count(workingDays))
                .mapToDouble(Double::valueOf)
                .sum();
    }
}
