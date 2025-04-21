package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.enums.PayslipRate;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipSettingDto;
import com.zsoft.gloody.gloodypayroll.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
        Object value = variables.getOrDefault(key, "0");
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0d;
        }
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

    public void setVariables(Employee employee, PayslipSettingDto dto, Map<String, Double> monthlyValues, Period period) {
        if (dto == null) {
            return;
        }
        variables = new LinkedHashMap<>(monthlyValues);
        double totalAbsences = monthlyValues.getOrDefault("ABS_TTL", 0d);
        double cpDays = monthlyValues.getOrDefault("ABS_CP", 0d);
        setVariable(PAYMENT_START, DateUtils.startOfMonth(period.start()));
        setVariable(PAYMENT_END, DateUtils.endOfMonth(period.end()));
        setVariable(IRG_YEAR, period.start().getYear());
        if (dto.getRate() != null) {
            setVariable(IS_DAILY_RATE, Objects.equals(dto.getRate(), PayslipRate.DAILY) ? 1 : 0);
            setVariable(IS_HOURLY_RATE, Objects.equals(dto.getRate(), PayslipRate.HOURLY) ? 1 : 0);
        }
        double countWorkingDays = findDoubleValue(DAYS_NBR) - totalAbsences;
        double hoursByDay = findDoubleValue(HOURS_BY_WEEK) / findDoubleValue(DAYS_BY_WEEK);
        setVariable(WORKING_DAYS_NBR, countWorkingDays - findDoubleValue(CONTRACT_OFF_DAYS));
        setVariable(WORKING_HOURS_NBR, (countWorkingDays * hoursByDay) - findDoubleValue(ABSENCES));
        if (Objects.equals(dto.getRate(), PayslipRate.DAILY)) {
            setVariable(ABSENCES, totalAbsences - cpDays);
        } else {
            setVariable(ABSENCES, (totalAbsences - cpDays) * hoursByDay);
        }
        setVariable(CP, cpDays);
        if (employee != null) {
            setVariable(BASE_SALARY, employee.getBaseSalary());
        }
        initialVariables.putAll(variables);
    }

    public void clearAllVariables(){
        if (variables != null){
            variables.clear();
        }
        if(initialVariables != null){
            initialVariables.clear();
        }
    }
}
