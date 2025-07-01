package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.Employee;
import com.zsoft.gloody.gloodypayroll.models.entities.Period;
import com.zsoft.gloody.gloodypayroll.models.enums.PayslipRate;
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
    private LinkedHashMap<String, Object> variables =  new LinkedHashMap<>();
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

    public void setVariableAndInitialVariable(String key, Object value) {
        variables.put(key, value);
        initialVariables.put(key, value);
    }

    public void setRate(PayslipRate rate) {
        if (rate != null) {
            setVariableAndInitialVariable(IS_DAILY_RATE, Objects.equals(rate , PayslipRate.DAILY) ? 1 : 0);
            setVariableAndInitialVariable(IS_HOURLY_RATE, Objects.equals(rate, PayslipRate.HOURLY) ? 1 : 0);
        }
    }

    public void setPeriod(Period period) {
        setVariableAndInitialVariable(PAYMENT_START, DateUtils.startOfMonth(period.start()));
        setVariableAndInitialVariable(PAYMENT_END, DateUtils.endOfMonth(period.end()));
        setVariableAndInitialVariable(IRG_YEAR, period.start().getYear());
    }


    public void setVariables(Employee employee,  Map<String, Double> monthlyValues,  Map<String, String> variablesMap) {
        variables = new LinkedHashMap<>(monthlyValues);
        double totalAbsences = monthlyValues.getOrDefault(variablesMap.getOrDefault("ABS_TTL", "ABS_TTL"), 0d);
        double countWorkingDays = findDoubleValue(variablesMap.getOrDefault(DAYS_NBR,  DAYS_NBR)) - totalAbsences;
        double hoursByDay = findDoubleValue(variablesMap.getOrDefault(HOURS_BY_WEEK, HOURS_BY_WEEK )) / findDoubleValue(variablesMap.getOrDefault(DAYS_BY_WEEK, DAYS_BY_WEEK ));
        setVariable(variablesMap.getOrDefault(WORKING_DAYS_NBR, WORKING_DAYS_NBR), countWorkingDays - findDoubleValue(variablesMap.getOrDefault(CONTRACT_OFF_DAYS, CONTRACT_OFF_DAYS )));
        setVariable(variablesMap.getOrDefault(WORKING_HOURS_NBR , WORKING_HOURS_NBR), (findDoubleValue(variablesMap.getOrDefault(WORKING_DAYS_NBR, WORKING_DAYS_NBR)) * hoursByDay));
        if (employee != null) {
            setVariable(variablesMap.getOrDefault(BASE_SALARY, BASE_SALARY ), employee.getBaseSalary());
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
