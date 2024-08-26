package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipAbsenceDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.rest.dto.VariableSettingDto;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import static com.zsoft.gloody.gloodypayroll.configs.Constants.*;

@RequiredArgsConstructor
public class SectionDictionaryService extends CompanySupportService {
    private LinkedHashMap<String, Object> variables;
    private LinkedHashMap<String, Object> initialVariables = new LinkedHashMap<>();
    private boolean addElements = true;

    public boolean getAddElements() {
        return addElements;
    }

    public void setAddElements(boolean addElements) {
        this.addElements = addElements;
    }

    public LinkedHashMap<String, Object> getInitialVariables() {
        return initialVariables;
    }

    @PostConstruct
    private void initVariables() {
        variables = new LinkedHashMap<>();
       /* settingRepository.findAll().forEach(setting -> {
            try {

                Object result = engine.eval(setting.getFormula());
                if (result != null) {
                    setVariable(setting.getKey(), valueOf(result));
                }
            } catch (ScriptException ignored) {
            }
        });*/
        setVariable(IRG_YEAR, LocalDate.now().getYear());
    }

    public Object findValue(String key) {
            return variables.get(key);
    }

    public Double findDoubleValue(String key) {
            return valueOf(variables.get(key));

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

    public void setVariables(PayslipSettingDto dto, Period period) {
        if (dto == null) {
            return;
        }
        variables.putAll(dto.getVariables()
                .stream()
                .collect(Collectors.
                        toMap(VariableSettingDto::getKey,
                                variableSettingDto -> {
                                     Double value = 0.0;
                                    if (variableSettingDto.getFormula() != null) {
                                        try {
                                            value =  Double.parseDouble(variableSettingDto.getFormula());
                                    } catch (Exception ignored) {
                                        }
                                    }
                                    return value;
                                })
                )
        );
        setVariable(PAYMENT_START, DateUtils.startOfMonth(period.getStart()));
        setVariable(PAYMENT_END, DateUtils.endOfMonth(period.getEnd()));
        setVariable(IRG_YEAR, period.getStart().getYear());
        // todo check this need
     /*   if (dto.getRate() != null) {
            setVariable(IS_DAILY_RATE, Objects.equals(dto.getRate(), PayslipRate.DAILY));
            setVariable(IS_HOURLY_RATE, Objects.equals(dto.getRate(), PayslipRate.HOURLY));
        }*/
        double countWorkingDays = findDoubleValue(DAYS_NBR) - dto.getAbsences().getAbsenceDays();
        double hoursByDay = findDoubleValue(HOURS_BY_WEEK) / findDoubleValue(DAYS_BY_WEEK);
        setVariable(WORKING_DAYS_NBR, countWorkingDays- findDoubleValue(CONTRACT_OFF_DAYS));
        setVariable(WORKING_HOURS_NBR, (countWorkingDays * hoursByDay) - findDoubleValue(ABSENCES));
        if ((boolean) findValue(IS_DAILY_RATE)) {
            setVariable(ABSENCES, countAbsences(dto.getAbsences()));
        } else {
            setVariable(ABSENCES, countAbsences(dto.getAbsences()) * hoursByDay);
        }
        setVariable(CP, countCP(dto.getAbsences()));
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

    private double countAbsences(PayslipAbsenceDto absences) {
        return  absences.getAbsenceDays() - absences.getCpDays();
    }

    private double countCP(PayslipAbsenceDto absences) {
        return absences.getCpDays();
    }
}
